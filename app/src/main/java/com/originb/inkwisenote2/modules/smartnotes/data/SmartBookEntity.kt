package com.originb.inkwisenote2.modules.smartnotes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_books")
class SmartBookEntity {
    // Getters and Setters
    @JvmField
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "book_id")
    var bookId: Long = 0

    @JvmField
    @ColumnInfo(name = "title")
    var title: String? = null


    @JvmField
    @ColumnInfo(name = "created_time_ms")
    var createdTimeMillis: Long = 0

    @JvmField
    @ColumnInfo(name = "last_modified_time_ms")
    var lastModifiedTimeMillis: Long = 0

    // No-args constructor required by Room
    constructor()

    // All-args constructor
    constructor(bookId: Long, title: String?, createdTimeMillis: Long, lastModifiedTimeMillis: Long) {
        this.bookId = bookId
        this.title = title
        this.createdTimeMillis = createdTimeMillis
        this.lastModifiedTimeMillis = lastModifiedTimeMillis
    }
}