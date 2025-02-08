package com.originb.inkwisenote.ux.activities.smartnotebook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.adapters.NoteGridAdapter;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.repositories.SmartNotebookRepository;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SmartNotebookAdapter extends RecyclerView.Adapter<NoteHolder> {

    private final ComponentActivity parentActivity;

    private final SmartNotebook smartNotebook;
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
        noteHolder.setNote(atomicNote);
        noteCards.put(atomicNote.getNoteId(), noteHolder);
    }

    @Override
    public int getItemCount() {
        return smartNotebook.atomicNotes.size();
    }
}
