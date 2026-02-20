package com.originb.inkwisenote2.modules.queries.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import com.originb.inkwisenote2.modules.queries.data.QueryEntity
import com.originb.inkwisenote2.modules.queries.data.QueryRepository
import com.originb.inkwisenote2.modules.smarthome.QueryNoteResult
import com.originb.inkwisenote2.modules.smarthome.SmartHomePageViewModel

class QueryResultsViewModel(
    private val queryRepository: QueryRepository,
    private val smartHomePageViewModel: SmartHomePageViewModel
) : ViewModel() {
    private val queries = MutableLiveData<MutableList<QueryEntity?>?>(ArrayList<QueryEntity?>())
    private val currentQueryResults = MutableLiveData<MutableSet<QueryNoteResult?>?>(HashSet<QueryNoteResult?>())

    init {
        loadQueries()
    }

    private fun loadQueries() {
        execute(
            Runnable { queryRepository.getAllQueries() },
            Runnable { queries.setValue() }
        )
    }

    fun loadQueryResults(queryName: String?) {
        execute(Runnable {
            // Use the existing query results map from SmartHomePageViewModel
            val allResults =
                smartHomePageViewModel.getLiveQueryResults().getValue()

            if (allResults != null && allResults.containsKey(queryName)) {
                return@execute allResults.get(queryName)
            }
            HashSet<Any?>()
        }, Runnable { currentQueryResults.setValue() })
    }

    fun getQueries(): LiveData<MutableList<QueryEntity?>?> {
        return queries
    }

    fun getCurrentQueryResults(): LiveData<MutableSet<QueryNoteResult?>?> {
        return currentQueryResults
    }
}