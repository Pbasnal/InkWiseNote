package com.originb.inkwisenote2.modules.handwrittennotes.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "handwritten_notes",
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
public class HandwrittenNoteEntity {

    @PrimaryKey()
    @ColumnInfo(name = "note_id")
    private long noteId;

    @ColumnInfo(name = "book_id")
    private long bookId;

    @ColumnInfo(name = "bitmap_file_path")
    private String bitmapFilePath;

    @ColumnInfo(name = "bitmap_hash")
    private String bitmapHash;

    @ColumnInfo(name = "page_template_file_path")
    private String pageTemplateFilePath;

    @ColumnInfo(name = "page_template_hash")
    private String pageTemplateHash;

    @ColumnInfo(name = "created_time_ms")
    private long createdTimeMillis;

    @ColumnInfo(name = "last_modified_time_ms")
    private long lastModifiedTimeMillis;
}




















