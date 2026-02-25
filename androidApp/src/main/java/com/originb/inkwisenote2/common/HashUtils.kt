package com.originb.inkwisenote2.common

import android.graphics.Bitmap
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.security.MessageDigest

object HashUtils {
    @JvmStatic
    fun calculateSha256(data: ByteArray): String? {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(data)

            // Convert hash into hexadecimal string
            val hexString = StringBuilder()
            for (b in hashBytes) {
                val hex = Integer.toHexString(0xFF and b.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }

            return hexString.toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }

    @JvmStatic
    fun getBitmapHash(bitmap: Bitmap): String? {
        val bitmapStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream)
        return calculateSha256(bitmapStream.toByteArray())
    }

    @JvmStatic
    fun getPageTemplateHash(pageTemplate: PageTemplate?): String? {
        try {
            val byteStream = ByteArrayOutputStream()
            ObjectOutputStream(byteStream).use { objectStream ->
                objectStream.writeObject(pageTemplate) // Serialize the object
            }
            return calculateSha256(byteStream.toByteArray())
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }
}
