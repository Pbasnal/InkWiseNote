package com.originb.inkwisenote2.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;

public class BitmapFileIoUtils {
    public static void writeDataToDisk(String filePath, Bitmap bitmap) {
        File file = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<Bitmap> readBitmapFromFile(String filePath, BitmapScale bitmapScale) {
        File file = new File(filePath);
        float bitmapScaleValue = bitmapScale.getValue();
        if (bitmapScaleValue < 1) {
            return getScaledImage(filePath, bitmapScaleValue);
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        return Optional.ofNullable(bitmap);
    }

    public static void deleteBitmap(String bitmapPath) {
        File noteFile = new File(bitmapPath);
        noteFile.delete();
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, float bitmapScale) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        int newWidth = Math.round(originalWidth * bitmapScale);
        int newHeight = Math.round(originalHeight * bitmapScale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private static Optional<Bitmap> getScaledImage(String filePath, float scale) {
        File file = new File(filePath);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        options.inSampleSize = calculateInSampleSize(options, scale);
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        return Optional.ofNullable(bitmap);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, float scale) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        int reqHeight = (int) (height * scale);
        int reqWidth = (int) (width * scale);
        try {
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
        } catch (Exception ex) {
            Log.e("Sampling error", ex.getMessage());
        }
        return inSampleSize;
    }

    public static boolean areEqual(Bitmap bitmap1, Bitmap bitmap2) {
        // STEP 1: Check if both Bitmaps are null
        if (bitmap1 == null || bitmap2 == null) {
            return false; // One or both Bitmaps are null
        }

        // STEP 2: Check if dimensions are identical
        if (bitmap1.getWidth() != bitmap2.getWidth() || bitmap1.getHeight() != bitmap2.getHeight()) {
            return false;
        }
        // STEP 3: Convert bitmaps to byte arrays
        byte[] byteArray1 = getBitmapAsByteArray(bitmap1);
        byte[] byteArray2 = getBitmapAsByteArray(bitmap2);

        // STEP 4: Compare the byte arrays
        return Arrays.equals(byteArray1, byteArray2);
    }

    // Helper function to convert Bitmap to byte array
    private static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream); // Use PNG for lossless compression
        return byteArrayOutputStream.toByteArray();
    }
}
