package com.originb.inkwisenote.data.entities.noterelationdata;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import com.originb.inkwisenote.data.entities.notedata.AtomicNoteEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(tableName = "note_relation",
        foreignKeys = {
                @ForeignKey(entity = AtomicNoteEntity.class,
                        parentColumns = "note_id",
                        childColumns = "note_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = SmartBookEntity.class,
                        parentColumns = "book_id",
                        childColumns = "book_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = AtomicNoteEntity.class,
                        parentColumns = "note_id",
                        childColumns = "related_note_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = SmartBookEntity.class,
                        parentColumns = "book_id",
                        childColumns = "related_book_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class NoteRelation {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "note_id")
    private Long noteId;

    @ColumnInfo(name = "book_id")
    private Long bookId;

    @ColumnInfo(name = "related_note_id")
    private Long relatedNoteId;

    @ColumnInfo(name = "related_book_id")
    private Long relatedBookId;

    @ColumnInfo(name = "relation_type")
    private Integer relationType;

    public NoteRelation(Long noteId, Long relatedNoteId, Long bookId, Long relatedBookId, Integer relationType) {
        this.noteId = noteId;
        this.relatedNoteId = relatedNoteId;
        this.bookId = bookId;
        this.relatedBookId = relatedBookId;

        this.relationType = relationType;
    }
}
