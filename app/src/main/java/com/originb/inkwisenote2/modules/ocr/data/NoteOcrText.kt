package com.originb.inkwisenote2.modules.ocr.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import java.io.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "note_text")
class NoteOcrText(
    @field:ColumnInfo(name = "note_id") @field:PrimaryKey private val noteId: Long, @field:ColumnInfo(
        name = "note_hash"
    ) private val noteHash: String, @field:ColumnInfo(name = "extracted_text") private val extractedText: String?
) : Serializable {
    @ColumnInfo(name = "created_time_ms")
    private val createdTimeMillis = System.currentTimeMillis()

    @ColumnInfo(name = "last_modified_time_ms")
    private val lastModifiedTimeMillis = System.currentTimeMillis()

    val createDateTimeString: String
        get() {
            val instant = Instant.ofEpochMilli(createdTimeMillis)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())

            return formatter.format(instant)
        }
}
