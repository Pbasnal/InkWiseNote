package com.originb.inkwisenote2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.config.AppState
import com.originb.inkwisenote2.config.ConfigKeys
import com.originb.inkwisenote2.config.ConfigReader
import com.originb.inkwisenote2.modules.handwrittennotes.HandwrittenNoteEventListener
import com.originb.inkwisenote2.modules.noterelation.NoteRelationEventListener
import com.originb.inkwisenote2.modules.ocr.worker.NoteOcrEventListener
import com.originb.inkwisenote2.modules.repositories.Repositories
import com.originb.inkwisenote2.modules.smartnotes.SmartNotebookEventListener
import com.originb.inkwisenote2.modules.textnote.TextNoteListener

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

        AppState.Companion.updateState()


        Routing.HomePageActivity.openHomePageAndStartFresh(this)
    }

    private fun registerModules() {
        ConfigReader.Companion.fromContext(this)
        Repositories.Companion.registerRepositories(this)

        val rootNotesDirectory = filesDir.path
        ConfigReader.Companion.setRuntimeSetting(ConfigKeys.NOTES_ROOT_DIRECTORY, rootNotesDirectory)

        notebookEventListner = SmartNotebookEventListener()
        handwrittenNoteEventListener = HandwrittenNoteEventListener()
        noteRelationEventListener = NoteRelationEventListener()
        noteOcrEventListener = NoteOcrEventListener()
        textNoteListener = TextNoteListener()
    }
}
