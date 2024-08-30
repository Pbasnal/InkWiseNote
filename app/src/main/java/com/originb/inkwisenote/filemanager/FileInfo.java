package com.originb.inkwisenote.filemanager;

import java.util.HashMap;
import java.util.Map;

public abstract class FileInfo<T> {
    public String filePath;
    public FileType fileType;
    public T data;
    public Class<? extends T> clazz;
    public Map<String, Float> extraFields = new HashMap<>();

    public FileInfo(String filePath, FileType fileType, Class<T> clazz) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.data = null;
        this.clazz = clazz;
    }

    public FileInfo(String filePath, FileType fileType, T data) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.data = data;
        clazz = (Class<? extends T>) data.getClass();
    }

    public void setData(Object o) {
        if(clazz.isInstance(o)) {
            data =  clazz.cast(o);
        }
    }

    public Map<String, Float> getExtraFields() {
        return extraFields;
    }
}
