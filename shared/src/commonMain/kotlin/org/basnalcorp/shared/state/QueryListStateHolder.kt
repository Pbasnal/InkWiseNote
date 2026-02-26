package org.basnalcorp.shared.state

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.basnalcorp.shared.data.repository.QueryRepository
import org.basnalcorp.shared.domain.Query

/**
 * Shared state holder for the saved queries list.
 * Exposes [queries] as a Flow for Compose to collect.
 */
class QueryListStateHolder(
    private val queryRepository: QueryRepository
) {
    val queries: Flow<List<Query>> = flow {
        emit(queryRepository.getAll())
    }
}
