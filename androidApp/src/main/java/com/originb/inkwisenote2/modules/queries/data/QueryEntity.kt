package com.originb.inkwisenote2.modules.queries.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Required by Room, set default value for non-null field
@Entity(tableName = "queries")
class QueryEntity {
    // Getters and Setters
    @PrimaryKey
    @ColumnInfo(name = "name")
    var name: String = "" // query name/description

    @ColumnInfo(name = "words_to_find")
    var wordsToFind: String? = null // comma-separated words

    @ColumnInfo(name = "words_to_ignore")
    var wordsToIgnore: String? = null // comma-separated words

    @ColumnInfo(name = "created_time_ms")
    var createdTimeMillis: Long = 0
}