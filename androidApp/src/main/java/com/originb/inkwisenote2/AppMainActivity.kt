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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_main)
        openSmartHomePageAndStartFresh(this)
    }
}
