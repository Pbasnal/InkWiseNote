package com.originb.inkwisenote.data.repositories;

import java.io.File;

public class FolderItem implements Comparable<FolderItem> {

    private int id;
    private int parentId;
    private File file;
    private String folderName;

    public FolderItem(int id, int parentId, File file) {
        this.id = id;
        this.parentId = parentId;
        this.file = file;
        this.folderName = file.getName();
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

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    @Override
    public int compareTo(FolderItem folderItem) {
        return this.folderName.compareTo(folderItem.folderName);

    }
}
