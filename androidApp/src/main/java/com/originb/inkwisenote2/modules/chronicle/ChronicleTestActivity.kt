package com.originb.inkwisenote2.modules.chronicle

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCommandResult
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCore
import org.koin.java.KoinJavaComponent.get
import kotlin.OptIn

/**
 * Minimal test UI for ChronicleCore: add notebook and add note.
 * Verify by checking the file system at app storage root / notes /.
 */
class ChronicleTestActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chronicleCore = get<ChronicleCore>(ChronicleCore::class.java)
        setContent {
            val scope = rememberCoroutineScope()
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("Chronicle Test") })
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val name = "test-notebook-${System.currentTimeMillis()}"
                                when (val r = chronicleCore.createNotebook(name)) {
                                    is ChronicleCommandResult.Success ->
                                        Toast.makeText(this@ChronicleTestActivity, "Created: ${r.value.notebookId}", Toast.LENGTH_SHORT).show()
                                    is ChronicleCommandResult.Failure ->
                                        Toast.makeText(this@ChronicleTestActivity, "Error: ${r.message}", Toast.LENGTH_SHORT).show()
                                    is ChronicleCommandResult.FailButRetry ->
                                        Toast.makeText(this@ChronicleTestActivity, "Retry: ${r.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text("Add notebook")
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                val notebooks = chronicleCore.listNotebooks()
                                if (notebooks.isEmpty()) {
                                    Toast.makeText(this@ChronicleTestActivity, "Create a notebook first", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                val notebookId = notebooks.first().notebookId
                                when (val r = chronicleCore.createNote(notebookId, "Test note", "")) {
                                    is ChronicleCommandResult.Success ->
                                        Toast.makeText(this@ChronicleTestActivity, "Created note in $notebookId", Toast.LENGTH_SHORT).show()
                                    is ChronicleCommandResult.Failure ->
                                        Toast.makeText(this@ChronicleTestActivity, "Error: ${r.message}", Toast.LENGTH_SHORT).show()
                                    is ChronicleCommandResult.FailButRetry ->
                                        Toast.makeText(this@ChronicleTestActivity, "Retry: ${r.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text("Add note (in first notebook)")
                    }
                }
            }
        }
    }
}
