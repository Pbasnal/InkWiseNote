package com.originb.inkwisenote;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.originb.inkwisenote.config.ConfigKeys;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.config.AppState;
import com.originb.inkwisenote.modules.handwrittennotes.HandwrittenNoteEventListener;
import com.originb.inkwisenote.modules.noterelation.NoteRelationEventListener;
import com.originb.inkwisenote.modules.ocr.worker.NoteOcrEventListener;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.common.Routing;
import com.originb.inkwisenote.modules.smartnotes.SmartNotebookEventListener;

public class AppMainActivity extends AppCompatActivity {
    private SmartNotebookEventListener notebookEventListner;
    private HandwrittenNoteEventListener handwrittenNoteEventListener;
    private NoteRelationEventListener noteRelationEventListener;
    private NoteOcrEventListener noteOcrEventListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main);

        registerModules();

        AppState.updateState();


        Routing.HomePageActivity.openHomePageAndStartFresh(this);
    }

    private void registerModules() {
        ConfigReader.fromContext(this);
        Repositories.registerRepositories(this);

        String rootNotesDirectory = getFilesDir().getPath();
        ConfigReader.setRuntimeSetting(ConfigKeys.NOTES_ROOT_DIRECTORY, rootNotesDirectory);

        notebookEventListner = new SmartNotebookEventListener();
        handwrittenNoteEventListener = new HandwrittenNoteEventListener();
        noteRelationEventListener = new NoteRelationEventListener();
        noteOcrEventListener = new NoteOcrEventListener();
    }
}
