package com.originb.inkwisenote2.modules.queries.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.queries.data.QueryEntity
import com.originb.inkwisenote2.modules.queries.data.QueryRepository
import com.originb.inkwisenote2.modules.smarthome.QueryNoteResult
import com.originb.inkwisenote2.modules.smarthome.SmartHomePageViewModel
import java.util.concurrent.Callable
import java.util.function.Consumer

class QueryResultsViewModel(
    private val queryRepository: QueryRepository,
    private val smartHomePageViewModel: SmartHomePageViewModel
) : ViewModel() {
    private val queries = MutableLiveData<MutableList<QueryEntity>>(ArrayList())
    private val currentQueryResults = MutableLiveData<MutableSet<QueryNoteResult>>(HashSet())

    init {
        loadQueries()
    }

    private fun loadQueries() {
        BackgroundOps.execute(
            { queryRepository.allQueries },
            Consumer { queries.value = it }
        )
    }

    fun loadQueryResults(queryName: String) {
        BackgroundOps.execute(
            {
                val allResults = smartHomePageViewModel.getLiveQueryResults().value!!
                allResults[queryName]?.toMutableSet()
            },
            { results ->
                currentQueryResults.value = results ?: mutableSetOf()
            }
        )
    }

    fun getQueries(): LiveData<MutableList<QueryEntity>> {
        return queries
    }

    fun getCurrentQueryResults(): LiveData<MutableSet<QueryNoteResult>> {
        return currentQueryResults
    }
}