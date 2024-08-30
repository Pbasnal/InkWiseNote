package com.originb.inkwisenote.data.repositories;

import java.io.File;

public class FileItem {
    private int id;
    private int parentId;
    private File file;
    private String fileName;

    public FileItem(int id, int parentId, File file) {
        this.id = id;
        this.parentId = parentId;
        this.file = file;
        this.fileName = file.getName();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
