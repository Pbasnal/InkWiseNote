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
    private val wordsToFind = MutableLiveData<MutableList<String?>?>(ArrayList<String?>())
    private val wordsToIgnore = MutableLiveData<MutableList<String?>?>(ArrayList<String?>())
    private var currentQuery: QueryEntity? = null
    private val currentQueryName = MutableLiveData<String?>("")

    private val allQueries: MutableLiveData<MutableMap<String?, QueryEntity?>?>


    init {
        this.allQueries = MutableLiveData<MutableMap<String?, QueryEntity?>?>(HashMap<String?, QueryEntity?>())
    }

    fun getAllQueries(): LiveData<MutableMap<String?, QueryEntity?>?> {
        val allQueriesMap: MutableMap<String?, QueryEntity?> = HashMap<String?, QueryEntity?>()

        execute(Runnable { repository.getAllQueries() }, Runnable { queries ->
            for (query in queries) {
                allQueriesMap.put(query.getName(), query)
            }
            allQueries.setValue(allQueriesMap)
        })

        return allQueries
    }

    fun addWordToFind(word: String) {
        if (word.isEmpty()) return

        val currentList = wordsToFind.getValue()
        if (currentList != null && !currentList.contains(word)) {
            currentList.add(word)
            wordsToFind.setValue(currentList)
        }
    }

    fun addWordToIgnore(word: String) {
        if (word.isEmpty()) return

        val currentList = wordsToIgnore.getValue()
        if (currentList != null && !currentList.contains(word)) {
            currentList.add(word)
            wordsToIgnore.setValue(currentList)
        }
    }

    fun removeWordToFind(word: String?) {
        var currentList = wordsToFind.getValue()
        if (currentList == null) currentList = ArrayList<String?>()

        currentList.remove(word)
        wordsToFind.setValue(currentList)
    }

    fun removeWordToIgnore(word: String?) {
        var currentList = wordsToFind.getValue()
        if (currentList == null) currentList = ArrayList<String?>()

        currentList.remove(word)
        wordsToIgnore.setValue(currentList)
    }

    fun findQueryWithQueryName(queryName: String?, onQueryFetch: Observer<QueryEntity?>) {
        execute(
            Runnable { repository.getQueryByName(queryName) },
            Runnable { onQueryFetch.onChanged() })
    }

    fun saveQuery(queryName: String?) {
        execute(Runnable {
            if (isNewQuery(queryName)) {
                repository.saveQuery(queryName, wordsToFind.getValue(), wordsToIgnore.getValue())
            } else {
                repository.updateQuery(currentQuery, wordsToFind.getValue(), wordsToIgnore.getValue())
            }
            repository.getQueryByName(queryName)
        }, Runnable { query ->
            val allQueriesMap = allQueries.getValue()
            allQueriesMap!!.put(queryName, query)

            allQueries.setValue(allQueriesMap)

            clearCurrentQuery()
            EventBus.getDefault().post(QueryUpdated(allQueriesMap.get(queryName)))
        })
    }

    fun deleteQuery(queryToDelete: QueryEntity?) {
        execute(Runnable {
            repository.deleteQuery(queryToDelete)
            queryToDelete
        }, Runnable { queryToRemove ->
            val allQueriesMap = allQueries.getValue()
            allQueriesMap!!.remove(queryToRemove.getName())

            allQueries.setValue(allQueriesMap)
            EventBus.getDefault().post(QueryDeleted(queryToRemove))
        })
    }

    private fun isNewQuery(queryName: String?): Boolean {
        return currentQuery == null || currentQuery!!.getName() != queryName
    }

    fun loadQuery(query: QueryEntity) {
        currentQuery = query
        currentQueryName.setValue(query.getName())
        wordsToFind.setValue(repository.getWordsToFind(query))
        wordsToIgnore.setValue(repository.getWordsToIgnore(query))
    }

    fun onWordsToFindChange(owner: LifecycleOwner, observer: Observer<MutableList<String?>?>) {
        wordsToFind.observe(owner, observer)
    }

    fun onWordsToIgnoreChange(owner: LifecycleOwner, observer: Observer<MutableList<String?>?>) {
        wordsToIgnore.observe(owner, observer)
    }

    fun onQueryNameChange(owner: LifecycleOwner, observer: Observer<String?>) {
        currentQueryName.observe(owner, observer)
    }

    fun clearCurrentQuery() {
        currentQuery = null
        currentQueryName.setValue("")
        wordsToFind.setValue(ArrayList<String?>())
        wordsToIgnore.setValue(ArrayList<String?>())
    }
}