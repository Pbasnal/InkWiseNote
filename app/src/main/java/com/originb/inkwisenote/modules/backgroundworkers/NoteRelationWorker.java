package com.originb.inkwisenote.modules.backgroundworkers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.common.util.CollectionUtils;
import com.originb.inkwisenote.Logger;
import com.originb.inkwisenote.data.config.AppState;
import com.originb.inkwisenote.data.dao.noterelation.NoteRelationDao;
import com.originb.inkwisenote.data.dao.noteocr.NoteTermFrequencyDao;
import com.originb.inkwisenote.data.dao.notes.SmartBookPagesDao;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookPage;
import com.originb.inkwisenote.data.entities.noteocrdata.NoteTermFrequency;
import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.data.entities.noterelationdata.NoteRelation;
import com.originb.inkwisenote.io.utils.ListUtils;
import com.originb.inkwisenote.modules.functionalUtils.Try;
import com.originb.inkwisenote.modules.repositories.NoteRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
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
    private final SmartNotebookRepository smartNotebookRepository;
    private final SmartBookPagesDao smartBookPagesDao;

    private final Handler mainHandler;

    private final Integer TF_IDF_RELATION = 1;

    private final Logger logger = new Logger("NoteRelationWorker");

    public NoteRelationWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        this.smartBookPagesDao = Repositories.getInstance().getNotesDb().smartBookPagesDao();

        noteRepository = Repositories.getInstance().getNoteRepository();
        noteTfIdfLogic = new NoteTfIdfLogic(Repositories.getInstance());
        noteTermFrequencyDao = Repositories.getInstance().getNotesDb().noteTermFrequencyDao();

        noteRelationDao = Repositories.getInstance().getNotesDb().noteRelationDao();

        mainHandler = new Handler(Looper.getMainLooper());
    }


    @NotNull
    @Override
    public Result doWork() {

        Optional<Long> noteIdOpt = Try.to(() -> getInputData().getLong("note_id", -1), logger)
                .get();
        noteIdOpt.ifPresent(this::findRelatedNotes);

        Optional<Long> bookIdOpt = Try.to(() -> getInputData().getLong("book_id", -1), logger)
                .get();
        bookIdOpt.ifPresent(this::findRelatedNotesOfSmartBook);

        return Result.success();
    }

    public void findRelatedNotesOfSmartBook(long bookId) {
        logger.debug("Find related notes for bookId (new flow): " + bookId);

        Optional<SmartNotebook> smartBookOpt = smartNotebookRepository.getSmartNotebook(bookId);
        if (!smartBookOpt.isPresent()) {
            logger.error("SmartNotebook doesn't exists for bookId: " + bookId);
            return;
        }

        SmartNotebook smartNotebook = smartBookOpt.get();
        logger.debug("Notebook bookId: " + bookId + " contains number of notes: " + smartNotebook.getAtomicNotes().size());
        for (AtomicNoteEntity atomicNote : smartNotebook.getAtomicNotes()) {
            findRelatedNotes(smartNotebook.getSmartBook().getBookId(), atomicNote);
        }
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
            AppState.getInstance().updatedRelatedNotes(noteRelations);
        });

    }

    public Result findRelatedNotes(long noteId) {
        logger.debug("Find related notes for noteId (old flow): " + noteId);

        Optional<NoteEntity> noteEntityOpt = noteRepository.getNoteEntity(noteId);

        final Integer TF_IDF_RELATION = 1;
        final Integer CREATED_TOGETHER_RELATION = 2;

        noteEntityOpt.map(noteEntity -> {
            Set<NoteRelation> noteRelations = getNoteIdsRelatedByTfIdf(noteEntity.getNoteId()).stream().map(relatedNoteId ->
                            new NoteRelation(noteEntity.getNoteId(), relatedNoteId, -1L, -1L, TF_IDF_RELATION))
                    .collect(Collectors.toSet());
            noteRelations.addAll(getNotesRelatedByCreation(noteEntity).stream().map(relatedNoteId ->
                            new NoteRelation(noteEntity.getNoteId(), relatedNoteId, -1L, -1L, CREATED_TOGETHER_RELATION))
                    .collect(Collectors.toSet()));

            noteRelationDao.insertNoteRelatedNotes(noteRelations);
            mainHandler.post(() -> {
                // Code to be executed on the main thread
//                AppState.getInstance().updatedRelatedNotes(noteEntity.getNoteId(), noteRelations);
            });
            return noteRelations;
        }).orElse(new HashSet<>());

        return null;
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
