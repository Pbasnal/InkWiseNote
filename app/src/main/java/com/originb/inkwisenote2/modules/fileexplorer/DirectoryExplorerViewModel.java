package com.originb.inkwisenote2.modules.fileexplorer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectoryExplorerViewModel extends ViewModel {

    private final MutableLiveData<List<FileGroup>> _fileGroups = new MutableLiveData<>();
    public final LiveData<List<FileGroup>> fileGroups = _fileGroups;

    private final MutableLiveData<Boolean> _isRefreshing = new MutableLiveData<>();
    public final LiveData<Boolean> isRefreshing = _isRefreshing;

    private final MutableLiveData<String> _toolbarTitle = new MutableLiveData<>();
    public final LiveData<String> toolbarTitle = _toolbarTitle;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public final LiveData<String> toastMessage = _toastMessage;

    private File currentDirectory;
    private final Stack<File> navigationHistory = new Stack<>();

    // Timestamp pattern regex
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^(\\d{8,14})(?:[\\.-]|$)");

    /**
     * Initialize the explorer with a starting directory.
     */
    public void init(File initialDir) {
        if (currentDirectory == null) {
            currentDirectory = initialDir;
            loadCurrentDirectory();
        }
    }

    public void loadCurrentDirectory() {
        if (currentDirectory == null) return;

        _isRefreshing.setValue(true);
        _toolbarTitle.setValue(currentDirectory.getName().isEmpty() ? "Files" : currentDirectory.getName());

        BackgroundOps.execute(() -> {
            List<FileItem> fileItems = new ArrayList<>();
            File[] files = currentDirectory.listFiles();

            if (files != null && files.length > 0) {
                for (File file : files) {
                    fileItems.add(new FileItem(file));
                }
                return groupFilesByTimestamp(fileItems);
            }
            return new ArrayList<FileGroup>();
        }, result -> {
            _fileGroups.setValue(result);
            _isRefreshing.setValue(false);
        });
    }

    /**
     * Grouping logic moved from Activity to ViewModel for testability.
     */
    private List<FileGroup> groupFilesByTimestamp(List<FileItem> fileItems) {
        Map<String, List<FileItem>> groupMap = new HashMap<>();
        List<FileItem> ungroupedFiles = new ArrayList<>();

        for (FileItem item : fileItems) {
            if (item.isDirectory()) {
                ungroupedFiles.add(item);
                continue;
            }

            String timestamp = extractTimestamp(item.getName());
            if (timestamp.isEmpty()) {
                ungroupedFiles.add(item);
            } else {
                if (!groupMap.containsKey(timestamp)) {
                    groupMap.put(timestamp, new ArrayList<>());
                }
                groupMap.get(timestamp).add(item);
            }
        }

        List<FileGroup> result = new ArrayList<>();
        for (Map.Entry<String, List<FileItem>> entry : groupMap.entrySet()) {
            result.add(new FileGroup(entry.getKey(), entry.getValue()));
        }
        for (FileItem item : ungroupedFiles) {
            result.add(new FileGroup(item));
        }

        Collections.sort(result, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            if (a.isGroup() && !b.isGroup()) return -1;
            if (!a.isGroup() && b.isGroup()) return 1;
            if (a.isGroup() && b.isGroup()) return b.getTimestamp().compareTo(a.getTimestamp());
            return a.getGroupName().compareToIgnoreCase(b.getGroupName());
        });

        return result;
    }

    private String extractTimestamp(String filename) {
        if (filename == null) return "";
        Matcher matcher = TIMESTAMP_PATTERN.matcher(filename);
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * Navigation Logic
     */
    public void navigateInto(File directory) {
        navigationHistory.push(currentDirectory);
        currentDirectory = directory;
        loadCurrentDirectory();
    }

    public boolean navigateBack() {
        if (!navigationHistory.isEmpty()) {
            currentDirectory = navigationHistory.pop();
            loadCurrentDirectory();
            return true;
        }
        return false;
    }

    /**
     * Delete Logic
     */
    public void deleteFileGroup(FileGroup fileGroup) {
        BackgroundOps.execute(() -> {
            boolean success = true;
            if (fileGroup.isGroup()) {
                for (File file : fileGroup.getAllRawFiles()) {
                    if (!file.delete()) success = false;
                }
            } else {
                FileItem singleFile = fileGroup.getPrimaryFile();
                if (singleFile != null) {
                    if (singleFile.isDirectory()) {
                        success = deleteRecursive(singleFile.getFile());
                    } else {
                        success = singleFile.getFile().delete();
                    }
                }
            }
            return success;
        }, success -> {
            if (success) {
                _toastMessage.setValue("Deleted successfully");
                loadCurrentDirectory();
            } else {
                _toastMessage.setValue("Failed to delete some items");
            }
        });
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteRecursive(child)) return false;
                }
            }
        }
        return fileOrDirectory.delete();
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }
}