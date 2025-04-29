package com.originb.inkwisenote2.modules.fileexplorer;

import java.io.File;

/**
 * Model class representing a file or directory in the file explorer
 */
public class FileItem {
    private final File file;
    private final String name;
    private final boolean isDirectory;
    private final long lastModified;
    private final long size;

    public FileItem(File file) {
        this.file = file;
        this.name = file.getName();
        this.isDirectory = file.isDirectory();
        this.lastModified = file.lastModified();
        this.size = file.length();
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getSize() {
        return size;
    }

    public String getPath() {
        return file.getAbsolutePath();
    }
} 