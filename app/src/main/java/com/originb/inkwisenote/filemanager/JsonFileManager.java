package com.originb.inkwisenote.filemanager;

import java.io.*;

public class JsonFileManager {
    public static <T> void writeDataToDisk(FileInfo<T> fileInfo) {
        File file = new File(fileInfo.filePath);
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(fileInfo.data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> FileInfo<T> readDataFromDisk(FileInfo fileInfo) {
        File file = new File(fileInfo.filePath);

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            fileInfo.setData(ois.readObject());

            return fileInfo;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
