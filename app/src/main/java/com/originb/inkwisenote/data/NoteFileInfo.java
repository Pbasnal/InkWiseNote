package com.originb.inkwisenote.data;

import com.originb.inkwisenote.filemanager.FileInfo;
import com.originb.inkwisenote.filemanager.FileType;

public class NoteFileInfo extends FileInfo<Note> {
    public NoteFileInfo(String filePath, Note note) {
        super(filePath, FileType.NOTE, note);
    }

    public NoteFileInfo(String filePath) {
        super(filePath, FileType.NOTE, Note.class);
    }
}
