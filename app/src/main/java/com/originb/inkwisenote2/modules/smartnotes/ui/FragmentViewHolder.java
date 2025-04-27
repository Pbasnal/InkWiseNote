package com.originb.inkwisenote2.modules.smartnotes.ui;

import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;

import static com.originb.inkwisenote2.modules.smartnotes.data.NoteType.*;

class FragmentViewHolder extends RecyclerView.ViewHolder {
    private final Logger logger = new Logger(FragmentViewHolder.class.getName());

    FrameLayout fragmentContainer;
    NoteFragment noteFragment;
    SmartNotebookAdapter adapter;
    FragmentManager fragmentManager;

    public FragmentViewHolder(SmartNotebookAdapter smartNotebookAdapter, @NonNull View itemView,
                              AppCompatActivity parentActivity) {
        super(itemView);
        fragmentContainer = itemView.findViewById(R.id.note_fragment_container);
        this.adapter = smartNotebookAdapter;
        this.fragmentManager = parentActivity.getSupportFragmentManager();
    }

    public void setNote(SmartNotebook notebook, AtomicNoteEntity atomicNote, int position) {
        if (isCorrectFragmentAttached(atomicNote)
                && atomicNote.getNoteId() == noteFragment.atomicNote.getNoteId()) return;

        noteFragment = createFragmentByType(notebook, atomicNote, adapter);

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

    public static NoteFragment createFragmentByType(SmartNotebook notebook, AtomicNoteEntity atomicNote, SmartNotebookAdapter adapter) {
        NoteType noteType = NoteType.fromString(atomicNote.getNoteType());
        switch (noteType) {
            case TEXT_NOTE:
                return new TextNoteFragment(notebook, atomicNote);
            case HANDWRITTEN_PNG:
                return new HandwrittenNoteFragment(notebook, atomicNote);
            default:
                return new InitNoteFragment(notebook, atomicNote, adapter);
        }
    }

    public NoteHolderData getNoteHolderData() {
        return noteFragment.getNoteHolderData();
    }
}
