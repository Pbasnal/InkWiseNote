package com.originb.inkwisenote2.modules.smartnotes;

import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;
import com.originb.inkwisenote2.modules.backgroundjobs.Events;
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.koin.java.KoinJavaComponent;

public class SmartNotebookEventListener {
    private SmartNotebookRepository smartNotebookRepository;

    public SmartNotebookEventListener(SmartNotebookRepository smartNotebookRepository) {
        // Inject SmartNotebookRepository via Koin
        this.smartNotebookRepository = smartNotebookRepository;
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onDeleteNotebookCommand(Events.DeleteNotebookCommand deleteNotebookCommand) {
        BackgroundOps.execute(() -> BackgroundOps.execute(() ->
                smartNotebookRepository.deleteSmartNotebook(deleteNotebookCommand.smartNotebook)
        ));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onDeleteNoteCommand(Events.DeleteNoteCommand deleteNoteCommand) {
        BackgroundOps.execute(() -> BackgroundOps.execute(() -> {
                    smartNotebookRepository.deleteNoteFromBook(deleteNoteCommand.smartNotebook,
                            deleteNoteCommand.atomicNote);
                }
        ));
    }

}
