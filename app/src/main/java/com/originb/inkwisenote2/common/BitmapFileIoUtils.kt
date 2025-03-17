package com.originb.inkwisenote2.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

object BitmapFileIoUtils {
    fun writeDataToDisk(filePath: String?, bitmap: Bitmap?) {
        val file = File(filePath)
        try {
            FileOutputStream(file).use { fos ->
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun readBitmapFromFile(filePath: String, bitmapScale: Float): Optional<Bitmap> {
        val file = File(filePath)
        if (bitmapScale < 1) {
            return getScaledImage(filePath, bitmapScale)
        }
        val options = BitmapFactory.Options()
        options.inMutable = true

        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
        return Optional.ofNullable(bitmap)
    }

    fun deleteBitmap(bitmapPath: String?) {
        val noteFile = File(bitmapPath)
        noteFile.delete()
    }

    fun resizeBitmap(bitmap: Bitmap?, bitmapScale: Float): Bitmap {
        val originalWidth = bitmap!!.width
        val originalHeight = bitmap.height

        val newWidth = Math.round(originalWidth * bitmapScale)
        val newHeight = Math.round(originalHeight * bitmapScale)

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun getScaledImage(filePath: String, scale: Float): Optional<Bitmap> {
        val file = File(filePath)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, options)

        options.inSampleSize = calculateInSampleSize(options, scale)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
        return Optional.ofNullable(bitmap)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, scale: Float): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        var reqHeight = (height * scale).toInt()
        var reqWidth = (width * scale).toInt()
        try {
            if (reqHeight * reqWidth == 0) {
                reqHeight = height / 2
                reqWidth = width / 2
            }

            // requested size is smaller than the bitmap size
            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }
        } catch (ex: Exception) {
            Log.e("Sampling error", ex.message!!)
        }
        return inSampleSize
    }

    fun areEqual(bitmap1: Bitmap?, bitmap2: Bitmap?): Boolean {
        // STEP 1: Check if both Bitmaps are null
        if (bitmap1 == null || bitmap2 == null) {
            return false // One or both Bitmaps are null
        }

        // STEP 2: Check if dimensions are identical
        if (bitmap1.width != bitmap2.width || bitmap1.height != bitmap2.height) {
            return false
        }
        // STEP 3: Convert bitmaps to byte arrays
        val byteArray1 = getBitmapAsByteArray(bitmap1)
        val byteArray2 = getBitmapAsByteArray(bitmap2)

        // STEP 4: Compare the byte arrays
        return byteArray1.contentEquals(byteArray2)
    }

    // Helper function to convert Bitmap to byte array
    private fun getBitmapAsByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream) // Use PNG for lossless compression
        return byteArrayOutputStream.toByteArray()
    }
}
