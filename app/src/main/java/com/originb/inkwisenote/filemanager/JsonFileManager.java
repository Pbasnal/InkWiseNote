package com.originb.inkwisenote.filemanager;

import android.os.Build;
import android.util.Log;
import com.originb.inkwisenote.data.config.PageTemplate;
import com.originb.inkwisenote.data.serializer.ByteSerializer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class JsonFileManager {
    public static <T> void writeDataToDisk(String filePath, T data) {
        byte[] bytes = ByteSerializer.serialize(data);
        JsonFileManager.writeByteArrayToFile(bytes, filePath);
    }

    public static void writeByteArrayToFile(byte[] byteArray, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(byteArray);
        } catch (IOException e) {
            Log.e("JsonFileManager", "Failed to write byte array to file", e);
        }
    }

    public static <T> Optional<T> readDataFromDisk(String filePath, Class<T> clazz) {
        byte[] bytes = readByteArrayFromFile(filePath);
        return Optional.ofNullable(ByteSerializer.deserialize(bytes, clazz));
    }

    public static byte[] readByteArrayFromFile(String filePath) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Files.readAllBytes(Paths.get(filePath));
            }
        } catch (IOException e) {
            Log.e("JsonFileManager", "Failed to read byte array from file", e);
        }
        return new byte[0];
    }
}
