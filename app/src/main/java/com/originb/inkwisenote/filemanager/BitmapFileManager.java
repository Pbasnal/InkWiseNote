package com.originb.inkwisenote.filemanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.*;

public class BitmapFileManager {
    public static <T> void writeDataToDisk(FileInfo<T> fileInfo) {
        File file = new File(fileInfo.filePath);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Bitmap bitmap = (Bitmap) fileInfo.data;
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> FileInfo<T> readDataFromDisk(FileInfo fileInfo) {
        File file = new File(fileInfo.filePath);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        fileInfo.setData(bitmap);
//        if(fileInfo.clazz.isInstance(bitmap)) {
//            return fileInfo.clazz.cast(bitmap);
//        }

        return fileInfo;
    }
}
