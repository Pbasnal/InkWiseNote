package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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

import static com.originb.inkwisenote2.modules.smartnotes.data.NoteType.*;

public class SmartNotebookAdapter extends RecyclerView.Adapter<SmartNotebookAdapter.FragmentViewHolder> {

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
        AtomicNoteEntity atomicNote = smartNotebook.getAtomicNotes().get(indexOfUpdatedNote);
//        noteCards.get(atomicNote.getNoteId()).setNote(smartNotebook, atomicNote, indexOfUpdatedNote);
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
        FragmentViewHolder holder = new FragmentViewHolder(view, this, parentActivity);
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
        if (position != -1) {
            atomicNote.setNoteType(newNoteType);

            NoteFragment fragment = createFragmentByType(atomicNote.getNoteType());
            fragment.setAtomicNote(atomicNote);
            fragment.setBookId(smartNotebook.smartBook.getBookId());
            BackgroundOps.execute(() -> smartNotebookRepository.updateNotebook(smartNotebook, parentActivity));
            notifyItemChanged(position);
        }
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

    private NoteFragment createFragmentByType(String noteType) {
        if (TEXT_NOTE.toString().equals(noteType)) {
            return new TextNoteFragment();
        } else if (HANDWRITTEN_PNG.toString().equals(noteType)) {
            return new HandwrittenNoteFragment();
        }
        return new InitNoteFragment(this);
    }

    public NoteHolderData getNoteData(long noteId) {
        return noteCards.get(noteId).getNoteHolderData();
    }

    public void setNoteData(Integer index, AtomicNoteEntity currentNote) {
        if (noteCards.containsKey(currentNote.getNoteId())) {
            noteCards.get(currentNote.getNoteId()).setNote(smartNotebook, currentNote, index);
        }
    }

    static class FragmentViewHolder extends RecyclerView.ViewHolder {
        FrameLayout fragmentContainer;
        NoteFragment noteFragment;
        SmartNotebookAdapter adapter;
        FragmentManager fragmentManager;

        public FragmentViewHolder(@NonNull View itemView, SmartNotebookAdapter adapter, AppCompatActivity parentActivity) {
            super(itemView);
            fragmentContainer = itemView.findViewById(R.id.note_fragment_container);
            this.adapter = adapter;
            this.fragmentManager = parentActivity.getSupportFragmentManager();
        }

        public void setNote(SmartNotebook notebook, AtomicNoteEntity atomicNote, int position) {
            if (isCorrectFragmentAttached(atomicNote)
                    && atomicNote.getNoteId() == noteFragment.atomicNote.getNoteId()) return;

            noteFragment = createFragmentByType(atomicNote.getNoteType());
            noteFragment.setAtomicNote(atomicNote);
            noteFragment.setBookId(notebook.smartBook.getBookId());

            final int containerId = fragmentContainer.getId();
            itemView.post(() -> {
                if (!itemView.isAttachedToWindow()) {
                    logger.debug("View hasn't attached to window");
                    return;
                }
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                // Remove any existing fragment in this container
                Fragment existingFragment = fragmentManager.findFragmentById(containerId);
                if (existingFragment != null && existingFragment != noteFragment) {
                    transaction.remove(existingFragment);
                }
                final String fragmentTag = "fragment_" + position;
                // Add the fragment to this container
                if (noteFragment.isDetached()) {
                    transaction.attach(noteFragment);
                } else if (!noteFragment.isAdded()) {
                    transaction.add(containerId, noteFragment, fragmentTag);
                } else {
                    transaction.replace(containerId, noteFragment, fragmentTag);
                }
                try {
                    transaction.commitNowAllowingStateLoss();
                } catch (Exception ex) {
                    logger.exception("Failed to commit fragment transaction", ex);
                }
            });
        }

        private boolean isCorrectFragmentAttached(AtomicNoteEntity atomicNote) {
            if (noteFragment == null) return false;

            NoteHolderData noteHolderData = noteFragment.getNoteHolderData();

            switch (noteHolderData.noteType) {
                case TEXT_NOTE:
                    return TEXT_NOTE.equals(atomicNote.getNoteType());
                case HANDWRITTEN_PNG:
                    return HANDWRITTEN_PNG.equals(atomicNote.getNoteType());
                case NOT_SET:
                    return NOT_SET.equals(atomicNote.getNoteType());
                default:
                    return false;
            }
        }

        private NoteFragment createFragmentByType(String noteType) {
            if (TEXT_NOTE.equals(noteType)) {
                return new TextNoteFragment();
            } else if (HANDWRITTEN_PNG.equals(noteType)) {
                return new HandwrittenNoteFragment();
            }
            return new InitNoteFragment(adapter);
        }

        public NoteHolderData getNoteHolderData() {
            return noteFragment.getNoteHolderData();
        }
    }
}




































