package com.originb.inkwisenote.modules.smartnotes.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.common.Logger;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote.modules.backgroundjobs.WorkManagerBus;
import com.originb.inkwisenote.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote.modules.handwrittennotes.ui.HandwrittenNoteHolder;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SmartNotebookAdapter extends RecyclerView.Adapter<NoteHolder> {

    private Logger logger = new Logger("SmartNotebookAdapter");

    private final ComponentActivity parentActivity;

    @Setter
    private SmartNotebook smartNotebook;
    private final SmartNotebookRepository smartNotebookRepository;

    // noteId to card mapping
    private final Map<Long, NoteHolder> noteCards = new HashMap<>();

    public SmartNotebookAdapter(ComponentActivity parentActivity,
                                SmartNotebook smartNotebook) {
        this.parentActivity = parentActivity;
        this.smartNotebook = smartNotebook;
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
    }

    @NonNull
    @NotNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int position) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_drawing_layout, parent, false);
        return new HandwrittenNoteHolder(itemView, parentActivity, smartNotebookRepository);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NoteHolder noteHolder, int position) {
        AtomicNoteEntity atomicNote = smartNotebook.getAtomicNotes().get(position);
        noteHolder.setNote(smartNotebook.getSmartBook().getBookId(), atomicNote);
        noteCards.put(atomicNote.getNoteId(), noteHolder);
    }

    @Override
    public int getItemCount() {
        return smartNotebook.atomicNotes.size();
    }

    public void saveNote(String noteTitle) {
        if (smartNotebook == null) return;
        BackgroundOps.execute(() -> {
            for (NoteHolder noteHolder : noteCards.values()) {
                noteHolder.saveNote();
            }

            // update title
            smartNotebook.getSmartBook().setTitle(noteTitle);
            smartNotebookRepository.updateNotebook(smartNotebook);
        });
    }

    public void removeNoteCard(long noteId) {
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


}
