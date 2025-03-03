package com.originb.inkwisenote;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.originb.inkwisenote.config.ConfigKeys;
import com.originb.inkwisenote.config.ConfigReader;
import com.originb.inkwisenote.config.AppState;
import com.originb.inkwisenote.modules.handwrittennotes.HandwrittenNoteEventListner;
import com.originb.inkwisenote.modules.noterelation.NoteRelationEventListner;
import com.originb.inkwisenote.modules.repositories.NoteRelationRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;
import com.originb.inkwisenote.common.Routing;
import com.originb.inkwisenote.modules.smartnotes.SmartNotebookEventListner;

public class AppMainActivity extends AppCompatActivity {
    private SmartNotebookEventListner notebookEventListner;
    private HandwrittenNoteEventListner handwrittenNoteEventListner;
    private NoteRelationEventListner noteRelationEventListner;

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

        notebookEventListner = new SmartNotebookEventListner();
        handwrittenNoteEventListner = new HandwrittenNoteEventListner();
        noteRelationEventListner = new NoteRelationEventListner();
    }
}
