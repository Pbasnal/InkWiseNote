package com.originb.inkwisenote2.common;

import android.util.Log;

import java.io.*;
import java.util.Objects;

public class ByteSerializer {
    public static byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            Log.e("ByteSerializer", "Failed to serialize object", e);
        }
        return new byte[0];
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz) {
        if (Objects.isNull(data) || data.length == 0) {
            return null;
        }

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

            Object obj = objectInputStream.readObject();
            return clazz.cast(obj);
        } catch (IOException | ClassNotFoundException e) {
            Log.e("ByteSerializer", "Failed to deserialize object", e);
        }

        return null;
    }
}
