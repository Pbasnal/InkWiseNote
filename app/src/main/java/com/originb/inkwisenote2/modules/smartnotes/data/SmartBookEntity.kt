package com.originb.inkwisenote2.modules.smartnotes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(tableName = "smart_books")
class SmartBookEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "book_id")
    private val bookId: Long = 0

    @ColumnInfo(name = "title")
    private val title: String? = null


    @ColumnInfo(name = "created_time_ms")
    private val createdTimeMillis: Long = 0

    @ColumnInfo(name = "last_modified_time_ms")
    private val lastModifiedTimeMillis: Long = 0
}


