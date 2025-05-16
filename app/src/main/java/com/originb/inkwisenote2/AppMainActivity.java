package com.originb.inkwisenote2;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.originb.inkwisenote2.config.ConfigKeys;
import com.originb.inkwisenote2.config.ConfigReader;
import com.originb.inkwisenote2.config.AppState;
import com.originb.inkwisenote2.modules.bootstrap.NotebookBootstrapper;
import com.originb.inkwisenote2.modules.handwrittennotes.HandwrittenNoteEventListener;
import com.originb.inkwisenote2.modules.textnote.TextNoteListener;
import com.originb.inkwisenote2.modules.noterelation.NoteRelationEventListener;
import com.originb.inkwisenote2.modules.ocr.worker.NoteOcrEventListener;
import com.originb.inkwisenote2.modules.repositories.Repositories;
import com.originb.inkwisenote2.common.Routing;
import com.originb.inkwisenote2.modules.smartnotes.SmartNotebookEventListener;
import com.originb.inkwisenote2.modules.repositories.SmartNotebook;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.R;

import java.io.File;
import java.util.List;

public class AppMainActivity extends AppCompatActivity {
    private static final String TAG = "AppMainActivity";
    private final Logger logger = new Logger(TAG);
    
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
        
        // Bootstrap notebooks and notes from working directory
        bootstrapNotebooksAndNotes();

        Routing.HomePageActivity.openSmartHomePageAndStartFresh(this);
    }
    
    /**
     * Bootstrap notebooks and notes from the working directory
     */
    private void bootstrapNotebooksAndNotes() {
        try {
            // Get the root notes directory from config
            String rootNotesDirectory = this.getFilesDir().getPath();
            File workingDirectory = new File(rootNotesDirectory);
            
            // Initialize bootstrapper
            NotebookBootstrapper bootstrapper = new NotebookBootstrapper();
            
            // Bootstrap notebooks and notes
            List<NotebookBootstrapper.NotebookFolder> bootstrappedNotebooks = bootstrapper.bootstrapFromDirectory(this, workingDirectory);
            
            // Log bootstrap results
            logger.debug("Bootstrap completed. Found " + bootstrappedNotebooks.size() + " notebooks");

        } catch (Exception e) {
            logger.exception("Error during bootstrap process", e);
        }
    }

    private void registerModules() {
        registerRepos(this);
        registerConfigs(this);

        notebookEventListner = new SmartNotebookEventListener();
        handwrittenNoteEventListener = new HandwrittenNoteEventListener();
        noteRelationEventListener = new NoteRelationEventListener();
        noteOcrEventListener = new NoteOcrEventListener();
        textNoteListener = new TextNoteListener();
    }

    public static void registerRepos(AppCompatActivity appCompatActivity) {
        ConfigReader.fromContext(appCompatActivity);
        Repositories.registerRepositories(appCompatActivity);
    }

    public static void registerConfigs(AppCompatActivity appCompatActivity) {
        String rootNotesDirectory = appCompatActivity.getFilesDir().getPath();
        ConfigReader.setRuntimeSetting(ConfigKeys.NOTES_ROOT_DIRECTORY, rootNotesDirectory);
    }
}
