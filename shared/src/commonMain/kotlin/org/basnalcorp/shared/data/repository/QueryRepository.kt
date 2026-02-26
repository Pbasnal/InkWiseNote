package org.basnalcorp.shared.data.repository

import org.basnalcorp.shared.domain.Query

interface QueryRepository {
    fun insertOrReplace(query: Query)
    fun delete(name: String)
    fun get(name: String): Query?
    fun getAll(): List<Query>
}
