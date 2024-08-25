package com.originb.inkwisenote.noterepository;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.originb.inkwisenote.Note;
import com.originb.inkwisenote.filemanager.FileInfo;
import com.originb.inkwisenote.filemanager.FileType;

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

    public List<FileInfo> getNoteFilesToSave(Note note, Bitmap bitmap) {
        List<FileInfo> filesToWrite = new ArrayList<>();
        filesToWrite.add(new FileInfo(directory + "/" + note.getNoteName() + ".note", FileType.NOTE, note));
        filesToWrite.add(new FileInfo(directory + "/" + note.getBitmapName() + ".png", FileType.BITMAP, bitmap));

        return filesToWrite;
    }

    public List<FileInfo> getNoteFilesToLoad(String noteName) {
        List<FileInfo> filesToRead = new ArrayList<>();
        filesToRead.add(new FileInfo(directory + "/" + noteName + ".note", FileType.NOTE, Note.class));
        filesToRead.add(new FileInfo(directory + "/" + noteName + ".png", FileType.BITMAP, Bitmap.class));

        return filesToRead;
    }

    // Save Note object to disk
    public void saveNoteToDisk(Note note, Bitmap bitmap) throws Exception {

        String noteFileName = note.getNoteName() + ".note";
        File file = new File(directory, noteFileName);
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(note);
        }

        String noteBitmapFileName = note.getBitmapName() + ".png";
        file = new File(directory, noteBitmapFileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        }
    }

    // Load Note object from disk
    public Note loadNoteFromDisk(String noteName) throws Exception {
        assert !noteName.contains(".");

        noteName = noteName + ".note";

        Note note = null;
        File file = new File(directory, noteName);
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            note = (Note) ois.readObject();
        }

        return note;
    }

    public Bitmap loadBitmapFromDisk(String fileName) {
        assert !fileName.contains(".");
        fileName = fileName + ".png";

        File file = new File(directory, fileName);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    public Bitmap getScaledBitmap(String fileName, int reqWidth, int reqHeight) {
        assert !fileName.contains(".");
        fileName = fileName + ".png";

        File bitmapFile = new File(directory, fileName);
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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