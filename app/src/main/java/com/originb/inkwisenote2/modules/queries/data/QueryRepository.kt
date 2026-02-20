package com.originb.inkwisenote2.modules.queries.data

import android.database.sqlite.SQLiteConstraintException
import com.google.android.gms.common.util.CollectionUtils
import java.lang.String
import java.util.*
import kotlin.Boolean

class QueryRepository(private val queryDao: QueriesDao) {
    val allQueries: MutableList<QueryEntity?>?
        get() = queryDao.getAllQueries()

    fun getQueryByName(queryName: String?): QueryEntity? {
        return queryDao.getQuery(queryName)
    }

    fun saveQuery(name: String, wordsToFind: MutableList<String?>, wordsToIgnore: MutableList<String?>) {
        val query = QueryEntity()
        fillEntityWithData(query, name, wordsToFind, wordsToIgnore)
        query.setCreatedTimeMillis(System.currentTimeMillis())

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
        wordsToFind: MutableList<String?>,
        wordsToIgnore: MutableList<String?>
    ): QueryEntity {
        query.setName(name)
        query.setWordsToFind(String.join(",", wordsToFind))
        query.setWordsToIgnore(String.join(",", wordsToIgnore))
        return query
    }

    fun updateQuery(
        query: QueryEntity,
        wordsToFind: MutableList<kotlin.String?>,
        wordsToIgnore: MutableList<kotlin.String?>
    ) {
        query.setWordsToFind(String.join(",", wordsToFind))
        query.setWordsToIgnore(String.join(",", wordsToIgnore))
        queryDao.updateQuery(query)
    }

    fun deleteQuery(query: QueryEntity?) {
        queryDao.deleteQuery(query)
    }

    fun getWordsToFind(query: QueryEntity): MutableList<kotlin.String?> {
        if (query.getWordsToFind() == null || query.getWordsToFind().isEmpty()) {
            return ArrayList<kotlin.String?>()
        }
        return ArrayList<kotlin.String?>(
            Arrays.asList<kotlin.String?>(
                *query.getWordsToFind().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            )
        )
    }

    fun getWordsToIgnore(query: QueryEntity): MutableList<kotlin.String?> {
        if (query.getWordsToIgnore() == null || query.getWordsToIgnore().isEmpty()) {
            return ArrayList<kotlin.String?>()
        }
        return ArrayList<kotlin.String?>(
            Arrays.asList<kotlin.String?>(
                *query.getWordsToIgnore().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            )
        )
    }
}