package com.originb.inkwisenote.data;

import android.graphics.Bitmap;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.function.Function;

@Getter
@Setter
public class Note implements Serializable {
    private static final long serialVersionUID = 1L;
    public Function<String, Bitmap> bitmapLoader;
    private Long noteId;
    private String noteName;

    public Note(Long noteId) {
        this.noteId = noteId;
    }
}