package com.originb.inkwisenote2.modules.smarthome;

import android.graphics.Bitmap;
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity;
import com.originb.inkwisenote2.modules.smartnotes.data.NoteType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryNoteResult {
    private long noteId;
    private Bitmap noteImage;
    private String queryWord;
    private String noteText;
    private long lastModifiedMillis;
    private NoteType noteType;

    public QueryNoteResult(AtomicNoteEntity atomicNoteEntity) {
        noteId = atomicNoteEntity.getNoteId();
        if (atomicNoteEntity.getLastModifiedTimeMillis() != 0) {
            lastModifiedMillis = atomicNoteEntity.getLastModifiedTimeMillis();
        } else {
            lastModifiedMillis = atomicNoteEntity.getCreatedTimeMillis();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        QueryNoteResult note = (QueryNoteResult) obj;

        // Custom equality logic (e.g., comparing titles only)
        return noteId == note.noteId;
    }

    @Override
    public int hashCode() {
        // Use the same attributes as equals() to generate hashCode
        return ((Long)noteId).hashCode();
    }
}
