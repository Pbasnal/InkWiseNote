package com.originb.inkwisenote2.modules.smartnotes.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import org.greenrobot.eventbus.EventBus;

public abstract class NoteFragment extends Fragment {

    protected SmartNotebook smartNotebook;
    protected AtomicNoteEntity atomicNote;

    public NoteFragment(SmartNotebook smartNotebook, AtomicNoteEntity atomicNote) {
        this.smartNotebook = smartNotebook;
        this.atomicNote = atomicNote;
    }

    /**
     * Shows a confirmation dialog before deleting a note
     */
    protected void confirmDeleteNote() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    EventBus.getDefault().post(new Events.DeleteNoteCommand(
                            smartNotebook,
                            atomicNote
                    ));
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton("Cancel", null)
                .show();
    }

    public abstract NoteHolderData getNoteHolderData();
}
