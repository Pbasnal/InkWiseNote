package com.originb.inkwisenote.io.utils;

import android.os.Build;
import android.util.Log;
import com.google.gson.Gson;
import com.originb.inkwisenote.data.serializer.ByteSerializer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class BytesFileIoUtils {
    public static <T> void writeDataToDisk(String filePath, T data) {
        byte[] bytes = ByteSerializer.serialize(data);
        Gson gson = new Gson();
        String json = gson.toJson(data);

        BytesFileIoUtils.writeByteArrayToFile(json.getBytes(), filePath);
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
        Gson gson = new Gson();
        String json = readJsonFromFile(filePath);
        return Optional.ofNullable(gson.fromJson(json, clazz));

//        return Optional.ofNullable(ByteSerializer.deserialize(bytes, clazz));
    }

    public static String readJsonFromFile(String filePath) {
        String content = "";
        try {
            List<String> allLines = Files.readAllLines(Paths.get(filePath));
            for (String line : allLines) {
                content = content.concat(line);
            }
        } catch (IOException e) {
            Log.e("JsonFileManager", "Failed to read byte array from file", e);
        }
        return content;
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
