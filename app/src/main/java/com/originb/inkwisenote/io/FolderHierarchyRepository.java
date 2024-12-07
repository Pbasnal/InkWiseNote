package com.originb.inkwisenote.io;

import com.originb.inkwisenote.modules.repositories.DirectoryContents;
import com.originb.inkwisenote.modules.repositories.FileItem;
import com.originb.inkwisenote.modules.repositories.FolderItem;

import java.io.File;
import java.util.*;

public class FolderHierarchyRepository {
    private final File rootDirectory;

    public FolderHierarchyRepository(File directory) {
        this.rootDirectory = directory;
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The provided file is not a directory");
        }
    }

    public DirectoryContents getFilesInDirectory() {
        List<FolderItem> allFolders = new ArrayList<>();
        Queue<FolderItem> filesQueue = new PriorityQueue<>();
        FolderItem rootFolder = new FolderItem(0, 0, rootDirectory);
        filesQueue.add(rootFolder);
        allFolders.add(rootFolder);

        List<FileItem> allFiles = new ArrayList<>();

        while (!filesQueue.isEmpty()) {
            FolderItem folderItem = filesQueue.remove();

            File[] folderContents = folderItem.getFile().listFiles();
            if (Objects.isNull(folderContents) || folderContents.length == 0) {
                continue;
            }

            for (File folderContent : folderContents) {
                if (folderContent.isDirectory()) {
                    FolderItem childFolder = new FolderItem(allFolders.size(), folderItem.getId(), folderContent);
                    filesQueue.add(childFolder);
                    allFolders.add(childFolder);
                } else {
                    FileItem childFile = new FileItem(allFiles.size(), folderItem.getId(), folderContent);
                    allFiles.add(childFile);
                }
            }
        }


        return new DirectoryContents(allFolders, allFiles);
    }
}
