package com.originb.inkwisenote.modules.textnote;

import com.originb.inkwisenote.modules.backgroundjobs.Events;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.modules.repositories.SmartNotebook;
import com.originb.inkwisenote.modules.textnote.data.TextNotesDao;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TextNoteListener {

    private final TextNotesDao textNotesDao = Repositories.getInstance().getNotesDb().textNotesDao();

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotebookDelete(Events.NotebookDeleted notebookToDelete) {
        SmartNotebook smartNotebook = notebookToDelete.smartNotebook;
        textNotesDao.deleteTextNotes(smartNotebook.getSmartBook().getBookId());
    }
}
