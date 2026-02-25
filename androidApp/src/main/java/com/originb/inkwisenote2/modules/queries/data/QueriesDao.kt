package com.originb.inkwisenote2.modules.queries.data

import androidx.room.*

@Dao
interface QueriesDao {
    @get:Query("SELECT * FROM queries ORDER BY created_time_ms DESC")
    val allQueries: MutableList<QueryEntity>

    @Query("SELECT * FROM queries limit 1")
    fun hasAnyQuery(): MutableList<QueryEntity>

    @Query("SELECT * FROM queries WHERE name = :query_name")
    fun getQuery(query_name: String): QueryEntity?

    @Insert
    fun insertQuery(query: QueryEntity): Long

    @Update
    fun updateQuery(query: QueryEntity)

    @Delete
    fun deleteQuery(query: QueryEntity)

    @Query("SELECT * FROM queries WHERE name = :name")
    fun getQueryByName(name: String): QueryEntity
}
