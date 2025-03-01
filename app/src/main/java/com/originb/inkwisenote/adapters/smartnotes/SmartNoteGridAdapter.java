package com.originb.inkwisenote.adapters.smartnotes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.Logger;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.data.config.AppState;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.entities.noterelationdata.NoteRelation;
import com.originb.inkwisenote.io.utils.ListUtils;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class SmartNoteGridAdapter extends RecyclerView.Adapter<GridNoteCardHolder> {
    private Logger logger = new Logger("SmartNoteGridAdapter");

    private final ComponentActivity parentActivity;
    private final SmartNotebookRepository smartNotebookRepository;

    private List<SmartNotebook> smartNoteBooks;

    private final Map<Long, List<NoteRelation>> bookRelationMap = new HashMap<>();

    private final Map<Long, Long> noteToBookMap = new HashMap<>();

    private final Map<Long, GridNoteCardHolder> bookCards = new HashMap<>();

    public SmartNoteGridAdapter(ComponentActivity parentActivity, List<SmartNotebook> smartNotebooks) {
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
        this.parentActivity = parentActivity;
        this.smartNoteBooks = smartNotebooks;

        for (SmartNotebook smartNotebook : smartNotebooks) {
            for (AtomicNoteEntity atomicNote : smartNotebook.getAtomicNotes()) {
                noteToBookMap.put(atomicNote.getNoteId(), smartNotebook.getSmartBook().getBookId());
            }
        }

        AppState.getInstance().observeNoteRelationships(parentActivity, this::updateNoteRelations);
    }

    public void updateNoteRelations(Map<Long, List<NoteRelation>> updatedNoteRelationMap) {
        logger.debug("Updating note relations", updatedNoteRelationMap.keySet());

        for (Long noteId : updatedNoteRelationMap.keySet()) {
            Long bookId = noteToBookMap.getOrDefault(noteId, -1L);

            if (bookId == -1) {
                logger.debug("BookId mapping doesn't exist for noteId: " + noteId);
                continue;
            }
            GridNoteCardHolder bookHolder = bookCards.get(noteId);

            if (bookHolder == null) {
                logger.debug("Book holder doesn't exists for  bookId: " + bookId);
                continue;
            }

            List<NoteRelation> updatedRelations = updatedNoteRelationMap.getOrDefault(noteId, new ArrayList<>())
                    .stream().filter(noteRelation ->
                            noteRelation.getBookId() != -1L && noteRelation.getRelatedBookId() != -1
                    ).collect(Collectors.toList());

            boolean notify = !bookRelationMap.containsKey(bookId) && !updatedRelations.isEmpty();

            logger.debug("Update relations of bookId: " + bookId + " will notify?: " + notify, updatedRelations);

            if (!notify) continue;
            this.bookRelationMap.put(bookId, updatedRelations);

            bookHolder.updateNoteRelation(updatedRelations);
            bookHolder.notify();
        }
    }

    public void setSmartNoteBooks(List<SmartNotebook> smartNoteBooks) {
        this.smartNoteBooks = new ArrayList<>(smartNoteBooks);

        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public GridNoteCardHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);

        return new GridNoteCardHolder(this, itemView, parentActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GridNoteCardHolder gridNoteCardHolder, int position) {
        SmartNotebook smartNotebook = smartNoteBooks.get(position);

        logger.debug("Setting book at position: " + position, smartNotebook.getSmartBook());
        gridNoteCardHolder.setNote(smartNotebook);
        bookCards.put(smartNotebook.getSmartBook().getBookId(), gridNoteCardHolder);

        long bookId = smartNotebook.getSmartBook().getBookId();
        if (!bookRelationMap.containsKey(bookId)) {
            logger.debug("Book doesn't have any relations yet. bookId: " + bookId);
            return;
        }

        gridNoteCardHolder.updateNoteRelation(bookRelationMap.get(bookId));
    }

    @Override
    public int getItemCount() {
        return smartNoteBooks.size();
    }

    public void removeSmartNotebook(int adapterPosition) {
        smartNoteBooks.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
    }
}
