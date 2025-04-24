package com.originb.inkwisenote2.modules.smartnotes.ui;

import androidx.fragment.app.Fragment;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteHolderData;
import lombok.Setter;

public abstract class NoteFragment extends Fragment {

    @Setter
    protected long bookId;
    @Setter
    protected AtomicNoteEntity atomicNote;

    public abstract NoteHolderData getNoteHolderData();
}
