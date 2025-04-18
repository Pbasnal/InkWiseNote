package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.handwrittennotes.ui.HandwrittenNoteHolder;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import com.originb.inkwisenote2.modules.textnote.TextNoteHolder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SmartNotebookAdapter extends RecyclerView.Adapter<NoteHolder> {

    private Logger logger = new Logger("SmartNotebookAdapter");
    private final ComponentActivity parentActivity;

    @Setter
    private SmartNotebook smartNotebook;
    private final SmartNotebookRepository smartNotebookRepository;

    // noteId to card mapping
    private final Map<Long, NoteHolder> noteCards = new HashMap<>();

    private static final int VIEW_TYPE_INIT = 0;
    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_HANDWRITTEN = 2;

    public SmartNotebookAdapter(ComponentActivity parentActivity,
                                SmartNotebook smartNotebook) {
        this.parentActivity = parentActivity;
        this.smartNotebook = smartNotebook;
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
    }

    @Override
    public int getItemViewType(int position) {
        AtomicNoteEntity atomicNote = smartNotebook.getAtomicNotes().get(position);
        if (NoteType.NOT_SET.toString().equals(atomicNote.getNoteType())) {
            return VIEW_TYPE_INIT;
        } else if (NoteType.TEXT_NOTE.toString().equals(atomicNote.getNoteType())) {
            return VIEW_TYPE_TEXT;
        } else if (NoteType.HANDWRITTEN_PNG.toString().equals(atomicNote.getNoteType())) {
            return VIEW_TYPE_HANDWRITTEN;
        }
        return VIEW_TYPE_INIT;
    }

    @NonNull
    @NotNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case VIEW_TYPE_TEXT:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_text_layout, parent, false);
                return new TextNoteHolder(itemView, parentActivity, smartNotebookRepository);
            case VIEW_TYPE_HANDWRITTEN:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_drawing_layout, parent, false);
                return new HandwrittenNoteHolder(itemView, parentActivity, smartNotebookRepository);
            case VIEW_TYPE_INIT:
            default:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_init_layout, parent, false);
                return new InitNoteHolder(itemView, parentActivity, smartNotebookRepository, this);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NoteHolder noteHolder, int position) {
        AtomicNoteEntity atomicNote = smartNotebook.getAtomicNotes().get(position);
        noteHolder.setNote(smartNotebook.getSmartBook().getBookId(), atomicNote);
        noteCards.put(atomicNote.getNoteId(), noteHolder);
    }

    @Override
    public void onViewRecycled(@NonNull NoteHolder holder) {
        super.onViewRecycled(holder);
        BackgroundOps.execute(() ->
                        smartNotebookRepository.getSmartNotebooks(smartNotebook.smartBook.getBookId()),
                existingSmartNotebook -> {
                    if (existingSmartNotebook.isPresent()) {
                        holder.saveNote();
                    }
                }
        );

    }

    public void updateNoteType(AtomicNoteEntity atomicNote, String newNoteType) {
        int position = smartNotebook.getAtomicNotes().indexOf(atomicNote);
        if (position != -1) {
            atomicNote.setNoteType(newNoteType);
            BackgroundOps.execute(() -> smartNotebookRepository.updateNotebook(smartNotebook, parentActivity));
            notifyItemChanged(position);
        }
    }

    public void saveNote(String noteTitle) {
        if (smartNotebook == null) return;
        BackgroundOps.execute(() -> {
            for (NoteHolder noteHolder : noteCards.values()) {
                noteHolder.saveNote();
            }

            // update title
            smartNotebook.getSmartBook().setTitle(noteTitle);
            smartNotebookRepository.updateNotebook(smartNotebook, parentActivity);
        });
    }

    public void removeNoteCard(long noteId) {
        if (!noteCards.containsKey(noteId)) return;

        int position = noteCards.get(noteId).getAdapterPosition();
        noteCards.remove(noteId);
        notifyItemRemoved(position);
    }

    // this function assumes that either the smartNotebook has updated
// notes and pages or
// all new notes or pages are inserted after current index so that
// the note and page at this index is not affected.
    public void saveNotebookPageAt(int currentVisibleItemIndex, AtomicNoteEntity atomicNote) {
        if (!noteCards.containsKey(atomicNote.getNoteId())) {
            return;
        }

        NoteHolder noteHolder = noteCards.get(atomicNote.getNoteId());
        BackgroundOps.execute(noteHolder::saveNote);
    }

    @Override
    public int getItemCount() {
        return smartNotebook != null ? smartNotebook.atomicNotes.size() : 0;
    }

}
