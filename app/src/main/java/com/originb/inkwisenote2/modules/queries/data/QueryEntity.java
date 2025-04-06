package com.originb.inkwisenote2.modules.queries.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import lombok.Data;

@Data
@Entity(tableName = "queries")
public class QueryEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "words_to_find")
    private String wordsToFind; // comma-separated words

    @ColumnInfo(name = "words_to_ignore")
    private String wordsToIgnore; // comma-separated words

    @ColumnInfo(name = "created_time_ms")
    private long createdTimeMillis;

    @ColumnInfo(name = "name")
    private String name; // query name/description
} 