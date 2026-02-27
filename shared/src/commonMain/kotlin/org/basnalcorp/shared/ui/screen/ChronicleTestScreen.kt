package org.basnalcorp.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCommandResult
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCore
import org.basnalcorp.shared.ui.LayoutContext
import org.basnalcorp.shared.ui.nav.Route

/**
 * Minimal test UI for ChronicleCore: add notebook and add note.
 * Verify by opening File explorer (defaults to app storage / notes) to see notebooks on disk.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronicleTestScreen(
    context: LayoutContext,
    chronicleCore: ChronicleCore?,
    onBack: () -> Unit,
    onNavigate: ((Route) -> Unit)? = null,
    onShowToast: ((String) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val show = { msg: String -> onShowToast?.invoke(msg) ?: println(msg) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chronicle Test") },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
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
            if (chronicleCore == null) {
                Text("Chronicle not available on this platform.")
                return@Column
            }
            Button(
                onClick = {
                    scope.launch {
                        val name = "test-notebook-${Clock.System.now().toEpochMilliseconds()}"
                        when (val r = chronicleCore.createNotebook(name)) {
                            is ChronicleCommandResult.Success ->
                                show("Created: ${r.value.notebookId}")
                            is ChronicleCommandResult.Failure ->
                                show("Error: ${r.message}")
                            is ChronicleCommandResult.FailButRetry ->
                                show("Retry: ${r.message}")
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
                            show("Create a notebook first")
                            return@launch
                        }
                        val notebookId = notebooks.first().notebookId
                        when (val r = chronicleCore.createNote(notebookId, "Test note", "")) {
                            is ChronicleCommandResult.Success ->
                                show("Created note in $notebookId")
                            is ChronicleCommandResult.Failure ->
                                show("Error: ${r.message}")
                            is ChronicleCommandResult.FailButRetry ->
                                show("Retry: ${r.message}")
                        }
                    }
                }
            ) {
                Text("Add note (in first notebook)")
            }
            if (onNavigate != null) {
                Button(
                    onClick = { onNavigate(Route.FileExplorer(initialPath = null)) }
                ) {
                    Text("Open in File explorer")
                }
            }
        }
    }
}
