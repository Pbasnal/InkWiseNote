package com.originb.inkwisenote2

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.originb.inkwisenote2.config.AppState
import com.originb.inkwisenote2.config.ConfigKeys
import com.originb.inkwisenote2.config.ConfigReader
import com.originb.inkwisenote2.modules.handwrittennotes.HandwrittenNoteEventListener
import com.originb.inkwisenote2.modules.noterelation.NoteRelationEventListener
import com.originb.inkwisenote2.modules.ocr.worker.NoteOcrEventListener
import com.originb.inkwisenote2.modules.smartnotes.SmartNotebookEventListener
import com.originb.inkwisenote2.modules.textnote.TextNoteListener
import org.basnalcorp.shared.setAppSecrets
import org.basnalcorp.shared.setAppStorageRoot
import org.basnalcorp.shared.setDriverContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent

class InkWiseApplication : Application() {
    private var notebookEventListner: SmartNotebookEventListener? = null
    private var handwrittenNoteEventListener: HandwrittenNoteEventListener? = null
    private var noteRelationEventListener: NoteRelationEventListener? = null
    private var noteOcrEventListener: NoteOcrEventListener? = null
    private var textNoteListener: TextNoteListener? = null


    override fun onCreate() {
        super.onCreate()

        // Phase 2: provide platform actuals to shared module
        setAppStorageRoot(this)
        setDriverContext(this)
        setAppSecrets(BuildConfig.VISION_API_KEY, BuildConfig.VISION_API_ENDPOINT)

        // Initialize WorkManager with Koin factory before any other initialization
        // This prevents the "WorkManager is already initialized" error
        val workManagerConfiguration = Configuration.Builder()
            .setWorkerFactory(getKoinWorkManagerFactory())
            .build()
        WorkManager.initialize(this, workManagerConfiguration)

        registerConfigs()

        AppState.updateState()

        startKoin {
            androidContext(applicationContext)
            modules(appModule)
        }

        // WorkManager is now initialized in InkWiseApplication.onCreate() to prevent double initialization
        // Initialize event listeners after Koin is started so they can use dependency injection
        initializeEventListeners()
    }

    private fun initializeEventListeners() {
        // Get event listeners from Koin DI
        notebookEventListner =
            KoinJavaComponent.get<SmartNotebookEventListener?>(SmartNotebookEventListener::class.java)
        handwrittenNoteEventListener =
            KoinJavaComponent.get<HandwrittenNoteEventListener?>(HandwrittenNoteEventListener::class.java)
        noteRelationEventListener =
            KoinJavaComponent.get<NoteRelationEventListener?>(NoteRelationEventListener::class.java)
        noteOcrEventListener = KoinJavaComponent.get<NoteOcrEventListener?>(NoteOcrEventListener::class.java)
        textNoteListener = KoinJavaComponent.get<TextNoteListener?>(TextNoteListener::class.java)
    }

    private fun registerConfigs() {
        ConfigReader.fromContext(this)
        ConfigReader.setRuntimeSetting(ConfigKeys.NOTES_ROOT_DIRECTORY, this.filesDir.path)
    }
}
