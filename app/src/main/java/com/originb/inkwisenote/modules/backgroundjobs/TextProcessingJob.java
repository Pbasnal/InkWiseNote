package com.originb.inkwisenote.modules.backgroundjobs;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.util.Log;
import com.originb.inkwisenote.data.notedata.NoteOcrText;
import com.originb.inkwisenote.modules.backgroundjobs.data.TextProcessingJobStatus;
import com.originb.inkwisenote.modules.backgroundjobs.data.TextProcessingStage;
import com.originb.inkwisenote.io.sql.NoteTextContract;
import com.originb.inkwisenote.io.sql.TextProcessingJobContract;
import com.originb.inkwisenote.modules.commonutils.Either;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.tfidf.BiRelationalGraph;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TextProcessingJob extends AsyncTask<Void, Void, Void> {
    @Setter
    private boolean continueJob = true;

    private JobService jobService;
    private JobParameters jobParams;
    private BiRelationalGraph biRelationalGraph;

    private TextProcessingJobContract.TextProcessingDbQueries textProcessingJobDbHelper;
    private NoteTextContract.NoteTextDbHelper noteTextDbHelper;

    private String nullDocumentError = "null document";

    TextProcessingJob(JobService jobService, JobParameters jobParams) {
        this.jobService = jobService;
        this.jobParams = jobParams;
        this.biRelationalGraph = new BiRelationalGraph(Repositories.getInstance());

        textProcessingJobDbHelper = Repositories.getInstance().getTextProcessingJobDbHelper();
        noteTextDbHelper = Repositories.getInstance().getNoteTextDbHelper();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Perform your background task here
        Log.d("MyJobService", "Performing background task");
        TextProcessingJobStatus jobStatus = textProcessingJobDbHelper.readFirstNoteJobStatus();

        if (Objects.isNull(jobStatus)) return null;

        if (!TextProcessingStage.Tokenization.equals(jobStatus.getStage())) {
            return null;
        }

        List<NoteOcrText> noteOcrTexts = NoteTextContract.NoteTextQueries.readTextFromDb(jobStatus.getNoteId(), noteTextDbHelper);
        noteOcrTexts.stream()
                .map(note -> handleException(this::extractTermsFromNote, note))
                .map(eitherTerms -> handleException(this::createBiRelationalGraph, eitherTerms.result))
                .forEach(this::deleteTextJob);

        return null;
    }

    private void deleteTextJob(Either<Exception, Long> eitherResult) {
        textProcessingJobDbHelper.deleteJob(eitherResult.result);
    }

    private <T, R> Either<Exception, R> handleException(Function<T, R> function, T input) {
        try {
            return Either.result(function.apply(input));
        } catch (Exception ex) {
            Log.e("TextProcessingJob", "failed to process", ex);
            return Either.error(ex);
        }
    }

    private DocumentTerms extractTermsFromNote(NoteOcrText noteOcrText) {
        String text = noteOcrText.getExtractedText();
        if (text == null || text.isEmpty()) {
            return null; // Return an empty list for null or empty text
        }

        // Step 1: Normalize the text to lowercase
        text = text.toLowerCase();

        // Step 2: Remove non-alphanumeric characters except spaces
        text = text.replaceAll("[^a-z0-9\\s]", "");

        // Step 3: Split the text into terms by whitespace
        String[] terms = text.split("\\s+");

        // Step 4: Filter out empty strings (in case of extra spaces)
        DocumentTerms documentTerms = new DocumentTerms();
        documentTerms.documentId = noteOcrText.getNoteId();

        documentTerms.terms = new ArrayList<>();
        for (String term : terms) {
            if (!term.isEmpty()) {
                documentTerms.terms.add(term);
            }
        }

        return documentTerms;
    }

    private Long createBiRelationalGraph(DocumentTerms documentTerms) {
        if (Objects.isNull(documentTerms)) return null;
        biRelationalGraph.addOrUpdateNote(documentTerms.documentId,
                documentTerms.terms);

        return documentTerms.documentId;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        jobService.jobFinished(jobParams, true);
    }

    private static class DocumentTerms {
        protected Long documentId;
        protected List<String> terms;
    }
}
