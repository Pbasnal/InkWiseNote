package com.example.hellodroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import java.io.*;

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

    // Save Note object to disk
    public void saveNoteToDisk(Note note, String fileName) throws Exception {
        File file = new File(directory, fileName);
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(note);
        }
    }

    public void saveBitmapToDisk(Bitmap bitmap, String fileName) throws Exception {

        File file = new File(directory, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        }
    }

    // Load Note object from disk
    public Note loadNoteFromDisk(String fileName) throws Exception {
        File file = new File(directory, fileName);
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (Note) ois.readObject();
        }
    }

    public Bitmap loadBitmapFromDisk(String fileName) {
        File file = new File(directory, fileName);
        Bitmap bitmap = loadBitmapWithCompletePath(file.getAbsolutePath());
        return bitmap;
    }
    public static Bitmap loadBitmapWithCompletePath(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        return BitmapFactory.decodeFile(filePath);
    }

    public static Bitmap getScaledBitmap(String filePath, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
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
    public File[] listNotes() {
        return directory.listFiles((dir, name) -> name.endsWith(".note")
                //|| name.endsWith(".png") // use to debug if the png image was created or not
        );
    }
}