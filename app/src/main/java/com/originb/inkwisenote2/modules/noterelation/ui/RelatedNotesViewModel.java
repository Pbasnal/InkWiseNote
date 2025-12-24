package com.originb.inkwisenote2.modules.noterelation.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.originb.inkwisenote2.common.BitmapScale;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelation;
import com.originb.inkwisenote2.modules.noterelation.data.NoteRelationDao;
import com.originb.inkwisenote2.modules.noterelation.data.RelatedNotesUiState;
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import java.util.*;
import java.util.stream.Collectors;

public class RelatedNotesViewModel extends ViewModel {

    private final SmartNotebookRepository smartNotebookRepository;
    private final HandwrittenNoteRepository handwrittenNoteRepository;
    private final NoteRelationRepository noteRelationRepository;
    private final NoteRelationDao noteRelationDao;

    private final MutableLiveData<RelatedNotesUiState> _uiState = new MutableLiveData<>();
    public final LiveData<RelatedNotesUiState> uiState = _uiState;

    private final MutableLiveData<Boolean> _noteDeletedEvent = new MutableLiveData<>(false);
    public final LiveData<Boolean> noteDeletedEvent = _noteDeletedEvent;

    public RelatedNotesViewModel(SmartNotebookRepository smartNotebookRepo,
                                 HandwrittenNoteRepository handwrittenRepo,
                                 NoteRelationRepository relationRepo,
                                 NoteRelationDao relationDao) {
        this.smartNotebookRepository = smartNotebookRepo;
        this.handwrittenNoteRepository = handwrittenRepo;
        this.noteRelationRepository = relationRepo;
        this.noteRelationDao = relationDao;
    }

    public void loadRelatedNotes(long rootBookId) {
        BackgroundOps.execute(() -> {
            Optional<SmartNotebook> rootOpt = smartNotebookRepository.getSmartNotebooks(rootBookId);
            if (!rootOpt.isPresent()) return null;

            SmartNotebook rootBook = rootOpt.get();
            Set<Long> noteIds = rootBook.atomicNotes.stream()
                    .map(AtomicNoteEntity::getNoteId)
                    .collect(Collectors.toSet());

            List<NoteRelation> noteRelations = noteRelationDao.getRelatedNotesOf(noteIds);

            Set<Long> allBookIds = noteRelations.stream()
                    .map(NoteRelation::getBookId).collect(Collectors.toSet());
            allBookIds.addAll(noteRelations.stream()
                    .map(NoteRelation::getRelatedBookId).collect(Collectors.toSet()));

            allBookIds.remove(rootBook.getSmartBook().getBookId());

            List<SmartNotebook> relatedBooks = allBookIds.stream()
                    .map(smartNotebookRepository::getSmartNotebooks)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            AtomicNoteEntity firstNote = rootBook.getAtomicNotes().get(0);
            var image = handwrittenNoteRepository.getNoteImage(firstNote, BitmapScale.THUMBNAIL);

            return new RelatedNotesUiState(rootBook, image, new HashSet<>(noteRelations), relatedBooks);
        }, state -> {
            if (state != null) _uiState.setValue(state);
        });
    }

    public void deleteRootNote(SmartNotebook notebook) {
        BackgroundOps.execute(() -> {
            notebook.atomicNotes.forEach(note -> {
                handwrittenNoteRepository.deleteHandwrittenNote(note);
                noteRelationRepository.deleteNoteRelationData(note);
            });
            smartNotebookRepository.deleteSmartNotebook(notebook);
            return true;
        }, result -> _noteDeletedEvent.setValue(true));
    }
}