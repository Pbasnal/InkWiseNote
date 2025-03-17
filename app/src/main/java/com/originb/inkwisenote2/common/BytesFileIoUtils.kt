package com.originb.inkwisenote2.common

import android.os.Build
import android.util.Log
import com.google.gson.Gson
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object BytesFileIoUtils {
    fun <T> writeDataToDisk(filePath: String?, data: T) {
        val bytes = ByteSerializer.serialize(data)
        val gson = Gson()
        val json = gson.toJson(data)

        writeByteArrayToFile(json.toByteArray(), filePath)
    }

    fun writeByteArrayToFile(byteArray: ByteArray?, filePath: String?) {
        try {
            FileOutputStream(filePath).use { fos ->
                fos.write(byteArray)
            }
        } catch (e: IOException) {
            Log.e("JsonFileManager", "Failed to write byte array to file", e)
        }
    }

    fun <T> readDataFromDisk(filePath: String?, clazz: Class<T>?): Optional<T> {
        val bytes = readByteArrayFromFile(filePath) ?: return Optional.empty()

        val gson = Gson()
        val json = readJsonFromFile(filePath)
        return Optional.ofNullable(gson.fromJson(json, clazz))
    }

    fun readJsonFromFile(filePath: String?): String {
        var content = ""
        try {
            val allLines = Files.readAllLines(Paths.get(filePath))
            for (line in allLines) {
                content = content + line
            }
        } catch (e: IOException) {
            Log.e("JsonFileManager", "Failed to read byte array from file", e)
        }
        return content
    }

    fun readByteArrayFromFile(filePath: String?): ByteArray? {
        try {
            val path = Paths.get(filePath)
            if (!Files.exists(path)) return null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Files.readAllBytes(path)
            }
        } catch (e: IOException) {
            Log.e("JsonFileManager", "Failed to read byte array from file", e)
        }
        return ByteArray(0)
    }
}
