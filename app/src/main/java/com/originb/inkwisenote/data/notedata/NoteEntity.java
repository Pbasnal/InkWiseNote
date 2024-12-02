package com.originb.inkwisenote.data.notedata;

import android.graphics.Bitmap;
import com.originb.inkwisenote.data.config.PageTemplate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoteEntity {
    private Long noteId;
    private NoteMeta noteMeta;
    private Bitmap noteBitmap;
    private PageTemplate pageTemplate;
}
