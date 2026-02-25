package com.originb.inkwisenote2.modules.queries.ui

import android.app.Application
import androidx.lifecycle.*
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import com.originb.inkwisenote2.modules.backgroundjobs.Events.QueryDeleted
import com.originb.inkwisenote2.modules.backgroundjobs.Events.QueryUpdated
import com.originb.inkwisenote2.modules.queries.data.QueryEntity
import com.originb.inkwisenote2.modules.queries.data.QueryRepository
import org.greenrobot.eventbus.EventBus

class QueryViewModel(application: Application, private val repository: QueryRepository) :
    AndroidViewModel(application) {
    private val wordsToFind = MutableLiveData<MutableList<String>>(ArrayList<String>())
    private val wordsToIgnore = MutableLiveData<MutableList<String>>(ArrayList<String>())
    private var currentQuery: QueryEntity? = null
    private val currentQueryName = MutableLiveData<String>("")

    private val allQueries: MutableLiveData<MutableMap<String, QueryEntity>> =
        MutableLiveData<MutableMap<String, QueryEntity>>(HashMap<String, QueryEntity>())

    fun getAllQueries(): LiveData<MutableMap<String, QueryEntity>> {
        val allQueriesMap: MutableMap<String, QueryEntity> = HashMap<String, QueryEntity>()

        execute({ repository.allQueries }, { queries ->
            if (queries != null) {
                for (query in queries) {
                    allQueriesMap[query.name] = query
                }
                allQueries.value = allQueriesMap
            }
        })

        return allQueries
    }

    fun addWordToFind(word: String) {
        if (word.isEmpty()) return

        val currentList = wordsToFind.getValue()
        if (currentList != null && !currentList.contains(word)) {
            currentList.add(word)
            wordsToFind.value = currentList
        }
    }

    fun addWordToIgnore(word: String) {
        if (word.isEmpty()) return

        val currentList = wordsToIgnore.getValue()
        if (currentList != null && !currentList.contains(word)) {
            currentList.add(word)
            wordsToIgnore.value = currentList
        }
    }

    fun removeWordToFind(word: String?) {
        var currentList = wordsToFind.getValue()
        if (currentList == null) currentList = ArrayList<String>()

        currentList.remove(word)
        wordsToFind.value = currentList
    }

    fun removeWordToIgnore(word: String?) {
        var currentList = wordsToFind.getValue()
        if (currentList == null) currentList = ArrayList<String>()

        currentList.remove(word)
        wordsToIgnore.value = currentList
    }

    fun findQueryWithQueryName(queryName: String, onQueryFetch: Observer<QueryEntity>) {
        execute(
            { repository.getQueryByName(queryName) },
            { query -> onQueryFetch.onChanged(query!!) })
    }

    fun saveQuery(queryName: String) {
        execute({
            if (isNewQuery(queryName)) {
                repository.saveQuery(queryName, wordsToFind.value!!, wordsToIgnore.value!!)
            } else {
                repository.updateQuery(currentQuery!!, wordsToFind.value!!, wordsToIgnore.value!!)
            }
            repository.getQueryByName(queryName)
        }, { query ->
            val allQueriesMap = allQueries.getValue()
            allQueriesMap!![queryName] = query!!

            allQueries.value = allQueriesMap

            clearCurrentQuery()
            EventBus.getDefault().post(QueryUpdated(allQueriesMap.get(queryName)))
        })
    }

    fun deleteQuery(queryToDelete: QueryEntity) {
        execute({
            repository.deleteQuery(queryToDelete)
            queryToDelete
        }, { queryToRemove ->
            val allQueriesMap = allQueries.value
            allQueriesMap!!.remove(queryToRemove?.name)

            allQueries.value = allQueriesMap
            EventBus.getDefault().post(QueryDeleted(queryToRemove))
        })
    }

    private fun isNewQuery(queryName: String?): Boolean {
        return currentQuery == null || currentQuery!!.name != queryName
    }

    fun loadQuery(query: QueryEntity) {
        currentQuery = query
        currentQueryName.value = query.name
        wordsToFind.value = repository.getWordsToFind(query)
        wordsToIgnore.value = repository.getWordsToIgnore(query)
    }

    fun onWordsToFindChange(owner: LifecycleOwner, observer: Observer<MutableList<String>>) {
        wordsToFind.observe(owner, observer)
    }

    fun onWordsToIgnoreChange(owner: LifecycleOwner, observer: Observer<MutableList<String>>) {
        wordsToIgnore.observe(owner, observer)
    }

    fun onQueryNameChange(owner: LifecycleOwner, observer: Observer<String>) {
        currentQueryName.observe(owner, observer)
    }

    fun clearCurrentQuery() {
        currentQuery = null
        currentQueryName.value = ""
        wordsToFind.value = ArrayList<String>()
        wordsToIgnore.value = ArrayList<String>()
    }
}