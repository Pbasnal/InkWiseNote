package com.originb.inkwisenote.modules.backgroundworkers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.data.config.AppState;
import com.originb.inkwisenote.data.dao.NoteRelationDao;
import com.originb.inkwisenote.data.dao.NoteTermFrequencyDao;
import com.originb.inkwisenote.data.entities.notedata.NoteTermFrequency;
import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.data.entities.notedata.NoteRelation;
import com.originb.inkwisenote.modules.functionalUtils.Try;
import com.originb.inkwisenote.modules.repositories.NoteRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.tfidf.NoteTfIdfLogic;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NoteRelationWorker extends Worker {
    private final NoteRepository noteRepository;
    private final NoteTfIdfLogic noteTfIdfLogic;
    private final NoteTermFrequencyDao noteTermFrequencyDao;
    private final NoteRelationDao noteRelationDao;

    private final DebugContext debugContext = new DebugContext("NoteRelationWorker");

    public NoteRelationWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);

        noteRepository = Repositories.getInstance().getNoteRepository();
        noteTfIdfLogic = new NoteTfIdfLogic(Repositories.getInstance());
        noteTermFrequencyDao = Repositories.getInstance().getNotesDb().noteTermFrequencyDao();

        noteRelationDao = Repositories.getInstance().getNotesDb().noteRelationDao();
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        Optional<Long> noteIdOpt = Try.to(() -> getInputData().getLong("note_id", -1), debugContext).get();

        Optional<NoteEntity> noteEntityOpt = noteIdOpt.flatMap(noteRepository::getNoteEntity);

        final Integer TF_IDF_RELATION = 1;
        final Integer CREATED_TOGETHER_RELATION = 2;

        noteEntityOpt.map(noteEntity -> {
            List<NoteRelation> noteRelations = getNoteIdsRelatedByTfIdf(noteEntity).stream().map(relatedNoteId ->
                            new NoteRelation(noteEntity.getNoteId(), relatedNoteId, TF_IDF_RELATION))
                    .collect(Collectors.toList());
            noteRelations.addAll(getNotesRelatedByCreation(noteEntity).stream().map(relatedNoteId ->
                            new NoteRelation(noteEntity.getNoteId(), relatedNoteId, CREATED_TOGETHER_RELATION))
                    .collect(Collectors.toList()));

            noteRelationDao.insertNoteRelatedNotes(noteRelations);
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> {
                // Code to be executed on the main thread
                AppState.getInstance().updatedRelatedNotes(noteEntity.getNoteId(), noteRelations);
            });
            return noteRelations;
        }).orElse(new ArrayList<>());

        return null;
    }

    private Set<Long> getNoteIdsRelatedByTfIdf(NoteEntity noteEntity) {
        Map<String, Double> tfIdfScores = noteTfIdfLogic.getTfIdf(noteEntity.getNoteId());
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

    private Set<Long> getNotesRelatedByCreation(NoteEntity noteEntity) {
        Set<Long> visitedNotes = new HashSet<>();
        visitedNotes.add(noteEntity.getNoteId());

        Set<Long> frontierNotes = noteEntity.getNoteMeta().getNextNoteIds();
        frontierNotes.addAll(noteEntity.getNoteMeta().getPrevNoteIds());

        int maxIterationDepth = 10;
        int i = 0;
        while (!frontierNotes.isEmpty() && i < maxIterationDepth) {
            Set<Long> newFrontierNotes = getConnectedNotes(frontierNotes, visitedNotes);
            visitedNotes.addAll(frontierNotes);
            frontierNotes = newFrontierNotes;
            i++;
        }

        visitedNotes.remove(noteEntity.getNoteId());
        return visitedNotes;
    }

    // todo: Instead of this logic, have the logic while the note is being created.
    // And update all connected notes to have the entire list.
    // - this background worker makes sense if the notebooks are too big to handle during
    //      note creation
    private Set<Long> getConnectedNotes(Set<Long> noteIds, Set<Long> visitedNoteIds) {

        Stream<NoteEntity> noteEntities = noteIds.stream().map(noteRepository::getNoteEntity)
                .filter(Optional::isPresent)
                .map(Optional::get);
        Set<Long> frontierNotes = noteEntities.map(noteEntity -> {
                    Set<Long> connectedNotes = noteEntity.getNoteMeta().getNextNoteIds();
                    connectedNotes.addAll(noteEntity.getNoteMeta().getPrevNoteIds());
                    return connectedNotes;
                }).flatMap(Set::stream)
                .collect(Collectors.toSet());
        frontierNotes.removeAll(visitedNoteIds);

        return frontierNotes;
    }


}
