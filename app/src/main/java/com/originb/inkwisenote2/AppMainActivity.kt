package com.originb.inkwisenote2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.originb.inkwisenote2.common.Routing.HomePageActivity.openSmartHomePageAndStartFresh
import com.originb.inkwisenote2.config.AppState
import com.originb.inkwisenote2.config.ConfigKeys
import com.originb.inkwisenote2.config.ConfigReader
import com.originb.inkwisenote2.modules.handwrittennotes.HandwrittenNoteEventListener
import com.originb.inkwisenote2.modules.noterelation.NoteRelationEventListener
import com.originb.inkwisenote2.modules.ocr.worker.NoteOcrEventListener
import com.originb.inkwisenote2.modules.smartnotes.SmartNotebookEventListener
import com.originb.inkwisenote2.modules.textnote.TextNoteListener
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get

class AppMainActivity : AppCompatActivity() {
    private var notebookEventListner: SmartNotebookEventListener? = null
    private var handwrittenNoteEventListener: HandwrittenNoteEventListener? = null
    private var noteRelationEventListener: NoteRelationEventListener? = null
    private var noteOcrEventListener: NoteOcrEventListener? = null
    private var textNoteListener: TextNoteListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_main)

        registerModules()

        AppState.updateState()

        startKoin {
            androidContext(applicationContext)
            modules(appModule)
        }

        // WorkManager is now initialized in InkWiseApplication.onCreate() to prevent double initialization
        // Initialize event listeners after Koin is started so they can use dependency injection
        initializeEventListeners()

        openSmartHomePageAndStartFresh(this)
    }

    private fun registerModules() {
        registerRepos(this)
        registerConfigs(this)
    }

    private fun initializeEventListeners() {
        // Get event listeners from Koin DI
        notebookEventListner = get<SmartNotebookEventListener?>(SmartNotebookEventListener::class.java)
        handwrittenNoteEventListener = get<HandwrittenNoteEventListener?>(HandwrittenNoteEventListener::class.java)
        noteRelationEventListener = get<NoteRelationEventListener?>(NoteRelationEventListener::class.java)
        noteOcrEventListener = get<NoteOcrEventListener?>(NoteOcrEventListener::class.java)
        textNoteListener = get<TextNoteListener?>(TextNoteListener::class.java)
    }

    companion object {
        @JvmStatic
        fun registerRepos(appCompatActivity: AppCompatActivity) {
            ConfigReader.fromContext(appCompatActivity)
            // Repositories are now managed by Koin, no need to register them manually
        }

        @JvmStatic
        fun registerConfigs(appCompatActivity: AppCompatActivity) {
            val rootNotesDirectory = appCompatActivity.getFilesDir().getPath()
            ConfigReader.setRuntimeSetting(ConfigKeys.NOTES_ROOT_DIRECTORY, rootNotesDirectory)
        }
    }
}
