package com.originb.inkwisenote2.modules.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a group of files with the same timestamp prefix
 */
public class FileGroup {
    private String timestamp;
    private String groupName;
    private List<FileItem> files;
    private boolean isGroup;
    
    // Constructor for a group of files
    public FileGroup(String timestamp, List<FileItem> files) {
        this.timestamp = timestamp;
        this.files = files;
        this.isGroup = true;
        
        // Set a meaningful group name
        if (!files.isEmpty()) {
            // Use the first file's name but remove the timestamp to make it cleaner
            String firstFileName = files.get(0).getName();
            if (firstFileName.contains("-")) {
                this.groupName = firstFileName.substring(firstFileName.indexOf("-") + 1);
            } else {
                this.groupName = firstFileName;
            }
        } else {
            this.groupName = "Group " + timestamp;
        }
    }
    
    // Constructor for a single file (not part of a group)
    public FileGroup(FileItem singleFile) {
        this.files = new ArrayList<>();
        this.files.add(singleFile);
        this.isGroup = false;
        this.groupName = singleFile.getName();
        this.timestamp = "";
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public List<FileItem> getFiles() {
        return files;
    }
    
    public boolean isGroup() {
        return isGroup;
    }
    
    public int getFileCount() {
        return files.size();
    }
    
    public FileItem getPrimaryFile() {
        return files.isEmpty() ? null : files.get(0);
    }
    
    public boolean isDirectory() {
        FileItem primaryFile = getPrimaryFile();
        return primaryFile != null && primaryFile.isDirectory();
    }
    
    public List<File> getAllRawFiles() {
        List<File> rawFiles = new ArrayList<>();
        for (FileItem item : files) {
            rawFiles.add(item.getFile());
        }
        return rawFiles;
    }
} 