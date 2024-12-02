package com.originb.inkwisenote.modules.repositories;

import java.util.List;

public class DirectoryContents {
    private List<FolderItem> folders;
    private List<FileItem> files;

    public DirectoryContents(List<FolderItem> folders, List<FileItem> fileItems) {
        this.folders = folders;
        this.files = fileItems;
    }

    public List<FolderItem> getFolders() {
        return folders;
    }

    public void setFolders(List<FolderItem> folders) {
        this.folders = folders;
    }

    public List<FileItem> getFiles() {
        return files;
    }

    public void setFiles(List<FileItem> files) {
        this.files = files;
    }

}
