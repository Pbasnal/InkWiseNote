package com.originb.inkwisenote.filemanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.originb.inkwisenote.data.BitmapFileInfo;

import java.io.*;
import java.util.Map;
import java.util.Objects;

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

    public static <T> FileInfo<T> readDataFromDisk(FileInfo<T> fileInfo) {
        File file = new File(fileInfo.filePath);

        float bitmapScale = getScale(fileInfo);
        if (bitmapScale < 1) {
            return getScaledImage(fileInfo, bitmapScale);
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        fileInfo.setData(bitmap);

        return fileInfo;
    }

    private static <T> FileInfo<T> getScaledImage(FileInfo<T> fileInfo, float scale) {
        File file = new File(fileInfo.filePath);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        options.inSampleSize = calculateInSampleSize(options, scale);
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        fileInfo.setData(bitmap);

        return fileInfo;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, float scale) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        int reqHeight = (int) (height * scale);
        int reqWidth = (int) (width * scale);

        if (reqHeight * reqWidth == 0) {
            reqHeight = height / 2;
            reqWidth = width / 2;
        }

        // requested size is smaller than the bitmap size
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

    private static <T> float getScale(FileInfo<T> fileInfo) {
        Map<String, Float> extraFields = fileInfo.getExtraFields();
        if (Objects.isNull(extraFields)) return 1f;
        else {
            return extraFields.getOrDefault("scale", 1f);
        }
    }
}
