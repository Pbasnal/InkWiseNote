package org.basnalcorp.shared.data.repository.impl

import org.basnalcorp.shared.data.repository.QueryRepository
import org.basnalcorp.shared.db.NotesDatabase
import org.basnalcorp.shared.domain.Query

class QueryRepositoryImpl(private val db: NotesDatabase) : QueryRepository {

    override fun insertOrReplace(query: Query) {
        db.queriesQueries.insertOrReplaceQuery(
            name = query.name,
            words_to_find = query.wordsToFind,
            words_to_ignore = query.wordsToIgnore,
            created_time_ms = query.createdTimeMillis
        )
    }

    override fun delete(name: String) {
        db.queriesQueries.deleteQuery(name = name)
    }

    override fun get(name: String): Query? =
        db.queriesQueries.getQuery(name = name).executeAsOneOrNull()?.toDomain()

    override fun getAll(): List<Query> =
        db.queriesQueries.allQueries().executeAsList().map { it.toDomain() }
}

private fun org.basnalcorp.shared.db.Queries.toDomain() = Query(
    name = name,
    wordsToFind = words_to_find,
    wordsToIgnore = words_to_ignore,
    createdTimeMillis = created_time_ms
)
