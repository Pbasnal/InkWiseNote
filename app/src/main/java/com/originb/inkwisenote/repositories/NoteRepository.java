package com.originb.inkwisenote.repositories;

import android.graphics.Bitmap;
import com.originb.inkwisenote.data.BitmapFileInfo;
import com.originb.inkwisenote.data.NoteFileInfo;
import com.originb.inkwisenote.data.Note;
import com.originb.inkwisenote.data.PageTemplateFileInfo;
import com.originb.inkwisenote.data.config.PageTemplate;
import com.originb.inkwisenote.filemanager.FileInfo;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class NoteRepository {
    private final File directory;

    // Constructor to set the directory where notes will be saved
    public NoteRepository(File directory) {
        this.directory = directory;
        // Ensure the directory exists
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void deleteNoteFromDisk(String noteName) {
        assert !noteName.contains(".");

        String noteFileName = noteName + ".note";
        String noteBitmapFileName = noteName + ".png";

        File noteFile = new File(directory, noteFileName);
        File bitmapFile = new File(directory, noteBitmapFileName);

        noteFile.delete();
        bitmapFile.delete();
    }

    public List<FileInfo> getNoteFilesToSave(Note note, Bitmap bitmap, PageTemplate pageTemplate) {
        List<FileInfo> filesToWrite = new ArrayList<>();
        filesToWrite.add(new NoteFileInfo(directory + "/" + note.getNoteName() + ".note", note));
        filesToWrite.add(new BitmapFileInfo(directory + "/" + note.getBitmapName() + ".png", bitmap));
        filesToWrite.add(new PageTemplateFileInfo(directory + "/" + note.getNoteName() + ".pagetemplate", pageTemplate));

        return filesToWrite;
    }

    public List<FileInfo> getNoteFilesToLoad(String noteName) {
        List<FileInfo> filesToRead = new ArrayList<>();

        if (Objects.isNull(noteName)) return filesToRead;

        filesToRead.add(new NoteFileInfo(directory + "/" + noteName + ".note"));
        filesToRead.add(new BitmapFileInfo(directory + "/" + noteName + ".png"));
        filesToRead.add(new PageTemplateFileInfo(directory + "/" + noteName + ".pagetemplate"));

        return filesToRead;
    }

    public BitmapFileInfo getThumbnailInfo(String noteName) {
        return new BitmapFileInfo(directory + "/" + noteName + ".png", 0.1f);
    }


    // List all saved notes
    public List<String> listNoteNamesInDirectory() {
        return Arrays.stream(Objects.requireNonNull(directory.listFiles((dir, name) -> name.endsWith(".note")
                        //|| name.endsWith(".png") // use to debug if the png image was created or not
                )))
                .map(File::getName)
                .map(name -> name.substring(0, name.lastIndexOf('.')))
                .collect(Collectors.toList());
    }
}