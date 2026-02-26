package org.basnalcorp.shared.state

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.basnalcorp.shared.data.repository.SmartNotebookRepository
import org.basnalcorp.shared.domain.SmartNotebook

/**
 * Shared state holder for the notebook list screen.
 * Exposes [notebooks] as a Flow for Compose to collect.
 * In Phase 5 the app can replace this with a Flow from SQLDelight query.asFlow() for reactive updates.
 */
class NotebookListStateHolder(
    private val smartNotebookRepository: SmartNotebookRepository
) {
    val notebooks: Flow<List<SmartNotebook>> = flow {
        emit(smartNotebookRepository.getAll())
    }
}
