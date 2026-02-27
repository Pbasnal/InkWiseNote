package org.basnalcorp.shared.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.basnalcorp.shared.data.repository.SmartNotebookRepository
import org.basnalcorp.shared.domain.SmartNotebook
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCore
import org.basnalcorp.shared.systems.chroniclecore.ChronicleNotebook

/**
 * Shared state holder for the notebook list screen.
 * When [chronicleCore] is available, [chronicleNotebooks] exposes first 10 Chronicle notebooks (replaced flow).
 * Call [refreshChronicleNotebooks] when the home screen is shown to refresh the list.
 */
class NotebookListStateHolder(
    private val smartNotebookRepository: SmartNotebookRepository,
    private val chronicleCore: ChronicleCore?
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Legacy: all SmartNotebooks (used when Chronicle is not available). */
    val notebooks: Flow<List<SmartNotebook>> = flow {
        emit(smartNotebookRepository.getAll())
    }

    private val _chronicleNotebooks = MutableStateFlow<List<ChronicleNotebook>>(emptyList())
    /** First 10 Chronicle notebooks for the home list. Call [refreshChronicleNotebooks] to update. */
    val chronicleNotebooks: StateFlow<List<ChronicleNotebook>> = _chronicleNotebooks.asStateFlow()

    fun refreshChronicleNotebooks() {
        if (chronicleCore == null) return
        scope.launch {
            _chronicleNotebooks.value = chronicleCore.listNotebooks().take(10)
        }
    }
}
