package com.originb.inkwisenote2.modules.queries.data

import android.database.sqlite.SQLiteConstraintException
import com.google.android.gms.common.util.CollectionUtils

class QueryRepository(private val queryDao: QueriesDao) {
    val allQueries: MutableList<QueryEntity>
        get() = queryDao.allQueries

    fun getQueryByName(queryName: String): QueryEntity? {
        return queryDao.getQuery(queryName)
    }

    fun saveQuery(name: String, wordsToFind: MutableList<String>, wordsToIgnore: MutableList<String>) {
        val query = QueryEntity()
        fillEntityWithData(query, name, wordsToFind, wordsToIgnore)
        query.createdTimeMillis = System.currentTimeMillis()

        try {
            queryDao.insertQuery(query)
        } catch (e: SQLiteConstraintException) {
            // Name already exists, update instead
            queryDao.updateQuery(query)
        }
    }

    fun userHasAnyQuery(): Boolean {
        val queries = queryDao.hasAnyQuery()
        return !CollectionUtils.isEmpty(queries)
    }

    fun fillEntityWithData(
        query: QueryEntity,
        name: String,
        wordsToFind: MutableList<String>,
        wordsToIgnore: MutableList<String>
    ): QueryEntity {
        query.name = name
        query.wordsToFind = wordsToFind.joinToString(",")
        query.wordsToIgnore = wordsToIgnore.joinToString(",")
        return query
    }

    fun updateQuery(
        query: QueryEntity,
        wordsToFind: MutableList<String>,
        wordsToIgnore: MutableList<String>
    ) {
        query.wordsToFind = wordsToFind.joinToString(",")
        query.wordsToIgnore = wordsToIgnore.joinToString(",")
        queryDao.updateQuery(query)
    }

    fun deleteQuery(query: QueryEntity?) {
        query?.let { queryDao.deleteQuery(it) }
    }

    fun getWordsToFind(query: QueryEntity): MutableList<String> {
        val words = query.wordsToFind
        if (words.isNullOrEmpty()) return mutableListOf()
        return words.split(",").dropLastWhile { it.isEmpty() }.toMutableList()
    }

    fun getWordsToIgnore(query: QueryEntity): MutableList<String> {
        val words = query.wordsToIgnore
        if (words.isNullOrEmpty()) return mutableListOf()
        return words.split(",").dropLastWhile { it.isEmpty() }.toMutableList()
    }
}