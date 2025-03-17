package com.originb.inkwisenote2.common

import android.util.Log
import java.io.*
import java.util.*

object ByteSerializer {
    fun serialize(obj: Any?): ByteArray {
        try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
                    objectOutputStream.writeObject(obj)
                    return byteArrayOutputStream.toByteArray()
                }
            }
        } catch (e: IOException) {
            Log.e("ByteSerializer", "Failed to serialize object", e)
        }
        return ByteArray(0)
    }

    fun <T> deserialize(data: ByteArray, clazz: Class<T>): T? {
        if (Objects.isNull(data) || data.size == 0) {
            return null
        }

        try {
            ByteArrayInputStream(data).use { byteArrayInputStream ->
                ObjectInputStream(byteArrayInputStream).use { objectInputStream ->
                    val obj = objectInputStream.readObject()
                    return clazz.cast(obj)
                }
            }
        } catch (e: IOException) {
            Log.e("ByteSerializer", "Failed to deserialize object", e)
        } catch (e: ClassNotFoundException) {
            Log.e("ByteSerializer", "Failed to deserialize object", e)
        }

        return null
    }
}
