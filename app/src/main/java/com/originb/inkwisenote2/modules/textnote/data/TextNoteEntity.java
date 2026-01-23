package com.originb.inkwisenote2.modules.textnote.data;

import androidx.room.*;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "text_notes",
        foreignKeys = {
                @ForeignKey(entity = SmartBookEntity.class,
                        parentColumns = "book_id",
                        childColumns = "book_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = AtomicNoteEntity.class,
                        parentColumns = "note_id",
                        childColumns = "note_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index("book_id"),
                @Index("note_id")
        })
public class TextNoteEntity {

    @PrimaryKey()
    @ColumnInfo(name = "note_id")
    private long noteId;

    @ColumnInfo(name = "book_id")
    private long bookId;

    @ColumnInfo(name = "note_text")
    private String noteText;

    @ColumnInfo(name = "created_time_ms")
    private long createdTimeMillis;

    @ColumnInfo(name = "last_modified_time_ms")
    private long lastModifiedTimeMillis;

    public TextNoteEntity(long noteId, long bookId) {
        this.noteId = noteId;
        this.bookId = bookId;
        noteText = "";

        createdTimeMillis = System.currentTimeMillis();
        lastModifiedTimeMillis = System.currentTimeMillis();
    }
}




















