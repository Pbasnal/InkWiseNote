package com.originb.inkwisenote2.modules.smartnotes.ui;

import androidx.fragment.app.Fragment;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;

public abstract class NoteFragment extends Fragment {

    protected SmartNotebook smartNotebook;
    protected AtomicNoteEntity atomicNote;

    public NoteFragment(SmartNotebook smartNotebook, AtomicNoteEntity atomicNote) {
        this.smartNotebook = smartNotebook;
        this.atomicNote = atomicNote;
    }

    public abstract NoteHolderData getNoteHolderData();
}
