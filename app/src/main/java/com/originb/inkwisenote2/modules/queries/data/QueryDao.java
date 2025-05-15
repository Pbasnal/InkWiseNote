package com.originb.inkwisenote2.modules.queries.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.Collection;
import java.util.List;

@Dao
public interface QueryDao {
    @Query("SELECT * FROM queries ORDER BY created_time_ms DESC")
    List<QueryEntity> getAllQueries();

    @Query("SELECT * FROM queries limit 1")
    List<QueryEntity> hasAnyQuery();

    @Query("SELECT * FROM queries WHERE name = :query_name")
    QueryEntity getQuery(String query_name);

    @Insert
    long insertQuery(QueryEntity query);

    @Update
    void updateQuery(QueryEntity query);

    @Delete
    void deleteQuery(QueryEntity query);

    @Query("SELECT * FROM queries WHERE name = :name")
    QueryEntity getQueryByName(String name);

}