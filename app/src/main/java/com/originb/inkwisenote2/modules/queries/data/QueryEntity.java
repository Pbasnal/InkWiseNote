package com.originb.inkwisenote2.modules.queries.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import lombok.Data;

@Data
@Entity(tableName = "queries")
public class QueryEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    private String name; // query name/description

    @ColumnInfo(name = "words_to_find")
    private String wordsToFind; // comma-separated words

    @ColumnInfo(name = "words_to_ignore")
    private String wordsToIgnore; // comma-separated words

    @ColumnInfo(name = "created_time_ms")
    private long createdTimeMillis;

    public QueryEntity() {
        // Required by Room, set default value for non-null field
        this.name = "";
    }
}