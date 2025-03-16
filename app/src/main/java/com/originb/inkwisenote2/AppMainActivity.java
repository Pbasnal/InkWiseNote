package com.originb.inkwisenote2;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.originb.inkwisenote2.config.ConfigKeys;
import com.originb.inkwisenote2.config.ConfigReader;
import com.originb.inkwisenote2.config.AppState;
import com.originb.inkwisenote2.modules.handwrittennotes.HandwrittenNoteEventListener;
import com.originb.inkwisenote2.modules.textnote.TextNoteListener;
import com.originb.inkwisenote2.modules.noterelation.NoteRelationEventListener;
import com.originb.inkwisenote2.modules.ocr.worker.NoteOcrEventListener;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.modules.smartnotes.SmartNotebookEventListener;
import com.originb.inkwisenote2.R;

public class AppMainActivity extends AppCompatActivity {
    private SmartNotebookEventListener notebookEventListner;
    private HandwrittenNoteEventListener handwrittenNoteEventListener;
    private NoteRelationEventListener noteRelationEventListener;
    private NoteOcrEventListener noteOcrEventListener;
    private TextNoteListener textNoteListener;

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
        textNoteListener = new TextNoteListener();
    }
}
