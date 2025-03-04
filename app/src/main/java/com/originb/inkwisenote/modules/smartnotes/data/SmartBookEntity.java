package com.originb.inkwisenote.modules.smartnotes.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(tableName = "smart_books")
public class SmartBookEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "book_id")
    private long bookId;

    @ColumnInfo(name = "title")
    private String title;


    @ColumnInfo(name = "created_time_ms")
    private long createdTimeMillis;

    @ColumnInfo(name = "last_modified_time_ms")
    private long lastModifiedTimeMillis;
}


