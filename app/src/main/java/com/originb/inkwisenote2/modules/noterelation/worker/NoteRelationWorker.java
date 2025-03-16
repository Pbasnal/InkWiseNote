package com.originb.inkwisenote2.modules.noterelation.worker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.config.AppState;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation;
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao;
import com.originb.inkwisenote2.modules.noterelation.data.TextProcessingStage;
import com.originb.inkwisenote2.modules.noterelation.service.NoteTfIdfLogic;
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPagesDao;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPage;
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequency;
import com.originb.inkwisenote2.common.ListUtils;
import com.originb.inkwisenote2.functionalUtils.Try;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class NoteRelationWorker extends Worker {
    private final NoteTfIdfLogic noteTfIdfLogic;
    private final NoteTermFrequencyDao noteTermFrequencyDao;
    private final NoteRelationDao noteRelationDao;
    private final SmartNotebookRepository smartNotebookRepository;
    private final SmartBookPagesDao smartBookPagesDao;

    private final Handler mainHandler;

    private final Integer TF_IDF_RELATION = 1;

    private final Logger logger = new Logger("NoteRelationWorker");

    public NoteRelationWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        this.smartBookPagesDao = Repositories.getInstance().getNotesDb().smartBookPagesDao();

        noteTfIdfLogic = new NoteTfIdfLogic(Repositories.getInstance());
        noteTermFrequencyDao = Repositories.getInstance().getNotesDb().noteTermFrequencyDao();

        noteRelationDao = Repositories.getInstance().getNotesDb().noteRelationDao();

        mainHandler = new Handler(Looper.getMainLooper());
    }


    @NotNull
    @Override
    public Result doWork() {
        Try.to(() -> getInputData().getLong("book_id", -1), logger).get()
                .ifPresent(this::findRelatedNotesOfSmartBook);

        return Result.success();
    }

    public void findRelatedNotesOfSmartBook(long bookId) {
        logger.debug("Find related notes for bookId (new flow): " + bookId);

        Optional<SmartNotebook> smartBookOpt = smartNotebookRepository.getSmartNotebooks(bookId);
        if (!smartBookOpt.isPresent()) {
            logger.error("SmartNotebook doesn't exists for bookId: " + bookId);
            return;
        }

        SmartNotebook smartNotebook = smartBookOpt.get();
        logger.debug("Notebook bookId: " + bookId + " contains number of notes: " + smartNotebook.getAtomicNotes().size());
        for (AtomicNoteEntity atomicNote : smartNotebook.getAtomicNotes()) {
            findRelatedNotes(smartNotebook.getSmartBook().getBookId(), atomicNote);
        }

        EventBus.getDefault().post(new Events.NoteStatus(smartNotebook, TextProcessingStage.NOTE_READY));
    }

    public void findRelatedNotes(long bookId, AtomicNoteEntity noteEntity) {

        Set<Long> relatedNoteIds = getNoteIdsRelatedByTfIdf(noteEntity.getNoteId());
        relatedNoteIds.remove(noteEntity.getNoteId());
        if (relatedNoteIds.isEmpty()) {
            noteRelationDao.deleteByNoteId(noteEntity.getNoteId());
            return;
        }

        Map<Long, Long> noteToBookMap = smartBookPagesDao.getSmartBookPagesOfNote(relatedNoteIds).stream()
                .collect((Collectors.toMap(SmartBookPage::getNoteId, SmartBookPage::getBookId)));

        logger.debug("Related noteIds of bookId: " + bookId, ListUtils.listOf(noteEntity, relatedNoteIds));

        Set<NoteRelation> noteRelations = relatedNoteIds.stream()
                .filter(noteId -> noteId > 0)
                .filter(noteToBookMap::containsKey)
                .map(relatedNoteId ->
                        new NoteRelation(noteEntity.getNoteId(),
                                relatedNoteId,
                                bookId,
                                noteToBookMap.get(relatedNoteId),
                                TF_IDF_RELATION)
                )
                .collect(Collectors.toSet());

        noteRelationDao.deleteByNoteId(noteRelations.stream()
                .map(NoteRelation::getNoteId)
                .collect(Collectors.toList()));

        noteRelationDao.deleteByNoteId(noteRelations.stream()
                .map(NoteRelation::getRelatedNoteId)
                .collect(Collectors.toList()));

        noteRelationDao.insertNoteRelatedNotes(noteRelations);

        if (CollectionUtils.isEmpty(noteRelations)) return;

        mainHandler.post(() -> {
            // Code to be executed on the main thread
            AppState.updatedRelatedNotes(noteRelations);
        });

    }

    private Set<Long> getNoteIdsRelatedByTfIdf(long noteId) {
        Map<String, Double> tfIdfScores = noteTfIdfLogic.getTfIdf(noteId);
        Set<String> filteredTerms = new HashSet<>();
        for (String key : tfIdfScores.keySet()) {
            if (tfIdfScores.get(key) > 0.1) {
                filteredTerms.add(key);
            }
        }

        Set<Long> relatedNoteIds = noteTermFrequencyDao.getNoteIdsForTerms(filteredTerms)
                .stream().map(NoteTermFrequency::getNoteId)
                .collect(Collectors.toSet());

        return relatedNoteIds;
    }
}
