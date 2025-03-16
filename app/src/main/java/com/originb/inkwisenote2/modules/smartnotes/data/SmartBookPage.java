package com.originb.inkwisenote2.modules.smartnotes.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "smart_book_pages",
        foreignKeys = {
                @ForeignKey(entity = SmartBookEntity.class,
                        parentColumns = "book_id",
                        childColumns = "book_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = AtomicNoteEntity.class,
                        parentColumns = "note_id",
                        childColumns = "note_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class SmartBookPage {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "book_id")
    private long bookId;

    @ColumnInfo(name = "note_id")
    private long noteId;

    @ColumnInfo(name = "page_order")
    private int pageOrder;

    public SmartBookPage(long bookId, long noteId, int pageOrder) {
        this.bookId = bookId;
        this.noteId = noteId;
        this.pageOrder = pageOrder;
    }
}
