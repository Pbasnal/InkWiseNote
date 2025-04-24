package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartNotebookAdapter extends RecyclerView.Adapter<FragmentViewHolder> {

    private static Logger logger = new Logger("SmartNotebookAdapter");
    private final AppCompatActivity parentActivity;

    private SmartNotebook smartNotebook;
    private final SmartNotebookRepository smartNotebookRepository;

    // noteId to card mapping
    private final Map<Long, FragmentViewHolder> noteCards = new HashMap<>();

    public SmartNotebookAdapter(AppCompatActivity parentActivity,
                                SmartNotebook smartNotebook) {
        this.parentActivity = parentActivity;
        this.smartNotebook = smartNotebook;
        this.smartNotebookRepository = Repositories.getInstance().getSmartNotebookRepository();
    }

    public void setSmartNotebook(SmartNotebook smartNotebook) {
        this.smartNotebook = smartNotebook;
        notifyDataSetChanged();
    }

    public void setSmartNotebook(SmartNotebook smartNotebook, int indexOfUpdatedNote) {
        this.smartNotebook = smartNotebook;
        notifyItemInserted(indexOfUpdatedNote);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @NotNull
    @Override
    public FragmentViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_note_page, parent, false);

        // Create a truly unique ID for the fragment container
        FragmentViewHolder holder = new FragmentViewHolder(this, view, this, parentActivity);
        holder.fragmentContainer.setId(viewType + 1);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull FragmentViewHolder holder, int position) {
        List<AtomicNoteEntity> atomicNotes = smartNotebook.getAtomicNotes();

        if (position < 0 || position >= atomicNotes.size()) return;

        AtomicNoteEntity atomicNote = atomicNotes.get(position);
        holder.setNote(smartNotebook, atomicNotes.get(position), position);
        noteCards.put(atomicNote.getNoteId(), holder);
    }

    public void updateNoteType(AtomicNoteEntity atomicNote, String newNoteType) {
        if (smartNotebook == null) return;

        int position = smartNotebook.getAtomicNotes().indexOf(atomicNote);
        if (position == -1) {
            return;
        }
        atomicNote.setNoteType(newNoteType);
        BackgroundOps.execute(() -> smartNotebookRepository.updateNotebook(smartNotebook, parentActivity));
        notifyItemChanged(position);

    }

    public void removeNoteCard(long noteId) {
        if (smartNotebook == null || !noteCards.containsKey(noteId)) return;

        int position = noteCards.get(noteId).getAdapterPosition();
        noteCards.remove(noteId);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return smartNotebook != null ? smartNotebook.atomicNotes.size() : 0;
    }

    public NoteHolderData getNoteData(long noteId) {
        return noteCards.get(noteId).getNoteHolderData();
    }

    public void setNoteData(Integer index, AtomicNoteEntity currentNote) {
        if (noteCards.containsKey(currentNote.getNoteId())) {
            noteCards.get(currentNote.getNoteId()).setNote(smartNotebook, currentNote, index);
        }
    }
}




































