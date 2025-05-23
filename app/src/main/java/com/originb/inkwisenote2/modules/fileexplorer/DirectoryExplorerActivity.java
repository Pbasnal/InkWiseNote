package com.originb.inkwisenote2.modules.fileexplorer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.originb.inkwisenote2.R;
import com.originb.inkwisenote2.common.Logger;
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectoryExplorerActivity extends AppCompatActivity
        implements FileGroupAdapter.OnFileGroupClickListener, FileGroupAdapter.OnFileGroupDeleteListener {

    private RecyclerView recyclerView;
    private FileGroupAdapter fileGroupAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private Toolbar toolbar;

    private File currentDirectory;
    private final Stack<File> navigationHistory = new Stack<>();
    private final Logger logger = new Logger("DirectoryExplorerActivity");

    // File type constants
    private static final int FILE_TYPE_IMAGE = 1;
    private static final int FILE_TYPE_MARKDOWN = 2;
    private static final int FILE_TYPE_UNKNOWN = 0;

    // Timestamp pattern regex
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^(\\d{8,14})(?:[\\.-]|$)");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_explorer);

        // Initialize views
        recyclerView = findViewById(R.id.files_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        emptyView = findViewById(R.id.empty_view);
        toolbar = findViewById(R.id.toolbar);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadCurrentDirectory);

        // Set up RecyclerView with a GridLayoutManager that adjusts columns based on screen width
        setupRecyclerView();

        // Set initial directory to the app's files directory
        currentDirectory = getFilesDir();
        loadCurrentDirectory();
    }

    private void setupRecyclerView() {
        // Calculate the number of columns based on screen width
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int numColumns = (int) (dpWidth / 120); // Approximate width of each item is 120dp
        numColumns = Math.max(2, numColumns); // Ensure at least 2 columns

        // Create GridLayoutManager with the calculated number of columns
        GridLayoutManager layoutManager = new GridLayoutManager(this, numColumns);
        recyclerView.setLayoutManager(layoutManager);

        // Create and set adapter
        fileGroupAdapter = new FileGroupAdapter(this, new ArrayList<>(), this, this);
        recyclerView.setAdapter(fileGroupAdapter);
    }

    private void loadCurrentDirectory() {
        swipeRefreshLayout.setRefreshing(true);

        // Update toolbar title
        toolbar.setTitle(currentDirectory.getName().isEmpty() ?
                "Files" : currentDirectory.getName());

        BackgroundOps.execute(() -> {
            List<FileItem> fileItems = new ArrayList<>();
            File[] files = currentDirectory.listFiles();

            if (files != null && files.length > 0) {
                for (File file : files) {
                    fileItems.add(new FileItem(file));
                }

                // Group files by timestamp
                return groupFilesByTimestamp(fileItems);
            }

            return new ArrayList<FileGroup>();
        }, result -> {
            swipeRefreshLayout.setRefreshing(false);
            fileGroupAdapter.updateFileGroups(result);

            // Show empty view if no files
            if (result.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Group files by timestamp prefix in their names
     *
     * @param fileItems List of individual file items
     * @return List of file groups
     */
    private List<FileGroup> groupFilesByTimestamp(List<FileItem> fileItems) {
        Map<String, List<FileItem>> groupMap = new HashMap<>();
        List<FileItem> ungroupedFiles = new ArrayList<>();

        // First pass: group files by timestamp
        for (FileItem item : fileItems) {
            if (item.isDirectory()) {
                // Directories are treated as individual items
                ungroupedFiles.add(item);
                continue;
            }

            String timestamp = extractTimestamp(item.getName());
            if (timestamp.isEmpty()) {
                // Files without timestamp are treated as individual items
                ungroupedFiles.add(item);
            } else {
                // Add file to its timestamp group
                if (!groupMap.containsKey(timestamp)) {
                    groupMap.put(timestamp, new ArrayList<>());
                }
                groupMap.get(timestamp).add(item);
            }
        }

        // Second pass: create FileGroup objects
        List<FileGroup> result = new ArrayList<>();

        // Add grouped files
        for (Map.Entry<String, List<FileItem>> entry : groupMap.entrySet()) {
            result.add(new FileGroup(entry.getKey(), entry.getValue()));
        }

        // Add ungrouped files as individual items
        for (FileItem item : ungroupedFiles) {
            result.add(new FileGroup(item));
        }

        // Sort groups: directories first, then groups, then individual files
        Collections.sort(result, (a, b) -> {
            // Directories come first
            if (a.isDirectory() && !b.isDirectory()) {
                return -1;
            } else if (!a.isDirectory() && b.isDirectory()) {
                return 1;
            }

            // Groups come before individual files
            if (a.isGroup() && !b.isGroup()) {
                return -1;
            } else if (!a.isGroup() && b.isGroup()) {
                return 1;
            }

            // Sort groups by timestamp (descending to show newest first)
            if (a.isGroup() && b.isGroup()) {
                return b.getTimestamp().compareTo(a.getTimestamp());
            }

            // Sort individual files by name
            return a.getGroupName().compareToIgnoreCase(b.getGroupName());
        });

        return result;
    }

    /**
     * Extract timestamp from a filename
     *
     * @param filename The filename to analyze
     * @return The timestamp string, or empty string if no timestamp found
     */
    private String extractTimestamp(String filename) {
        if (filename == null) {
            return "";
        }

        // Try to extract timestamp using regex
        Matcher matcher = TIMESTAMP_PATTERN.matcher(filename);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    @Override
    public void onFileGroupClick(FileGroup fileGroup) {
        if (fileGroup.isDirectory()) {
            // Navigate into directory (single file group representing a directory)
            navigationHistory.push(currentDirectory);
            currentDirectory = fileGroup.getPrimaryFile().getFile();
            loadCurrentDirectory();
        } else if (fileGroup.isGroup()) {
            // For a group, show a list of files in the group
            showFileGroupContents(fileGroup);
        } else {
            // For a single file, show the file
            FileItem singleFile = fileGroup.getPrimaryFile();
            if (singleFile != null) {
                showFileByType(singleFile);
            }
        }
    }

    /**
     * Display a dialog showing the list of files in a file group
     *
     * @param fileGroup The file group to display
     */
    private void showFileGroupContents(FileGroup fileGroup) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Files in " + fileGroup.getGroupName());

        List<FileItem> files = fileGroup.getFiles();
        String[] fileNames = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            fileNames[i] = files.get(i).getName();
        }

        builder.setItems(fileNames, (dialog, which) -> {
            // When a file is selected, show it
            showFileByType(files.get(which));
        });

        builder.setPositiveButton("Close", null);
        builder.show();
    }

    /**
     * Show a file based on its type
     *
     * @param fileItem The file to show
     */
    private void showFileByType(FileItem fileItem) {
        int fileType = getFileType(fileItem.getName());
        switch (fileType) {
            case FILE_TYPE_IMAGE:
                showImageFile(fileItem.getFile());
                break;
            case FILE_TYPE_MARKDOWN:
                showMarkdownFile(fileItem.getFile());
                break;
            default:
                showUnknownFileError(fileItem.getName());
                break;
        }
    }

    @Override
    public void onFileGroupDelete(FileGroup fileGroup) {
        String message;
        if (fileGroup.isGroup()) {
            message = "Are you sure you want to delete all " + fileGroup.getFileCount() +
                    " files in group '" + fileGroup.getGroupName() + "'?";
        } else {
            FileItem singleFile = fileGroup.getPrimaryFile();
            if (singleFile == null) return;

            String itemType = singleFile.isDirectory() ? "directory" : "file";
            message = "Are you sure you want to delete " + itemType + " '" + singleFile.getName() + "'?";
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Confirmation")
                .setMessage(message)
                .setPositiveButton("Delete", (dialog, which) -> deleteFileGroup(fileGroup))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteFileGroup(FileGroup fileGroup) {
        BackgroundOps.execute(() -> {
            boolean success = true;

            if (fileGroup.isGroup()) {
                // Delete all files in the group
                List<File> files = fileGroup.getAllRawFiles();
                for (File file : files) {
                    if (!file.delete()) {
                        success = false;
                    }
                }
            } else {
                // Delete a single file or directory
                FileItem singleFile = fileGroup.getPrimaryFile();
                if (singleFile != null) {
                    if (singleFile.isDirectory()) {
                        success = deleteDirectory(singleFile.getFile());
                    } else {
                        success = singleFile.getFile().delete();
                    }
                }
            }

            return success;
        }, success -> {
            if (success) {
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                loadCurrentDirectory();
            } else {
                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        return false;
                    }
                }
            }
        }
        return directory.delete();
    }

    /**
     * Determine the file type based on file extension
     *
     * @param fileName Name of the file
     * @return Integer constant representing the file type
     */
    private int getFileType(String fileName) {
        if (fileName == null) {
            return FILE_TYPE_UNKNOWN;
        }

        String lowercaseName = fileName.toLowerCase();

        // Check for image file extensions
        if (lowercaseName.endsWith(".jpg") ||
                lowercaseName.endsWith(".jpeg") ||
                lowercaseName.endsWith(".png") ||
                lowercaseName.endsWith(".gif") ||
                lowercaseName.endsWith(".webp") ||
                lowercaseName.endsWith(".bmp")) {
            return FILE_TYPE_IMAGE;
        }

        // Check for markdown files
        if (lowercaseName.endsWith(".md") ||
                lowercaseName.endsWith(".markdown") ||
                lowercaseName.endsWith(".txt") ||
                lowercaseName.endsWith(".pt")) {
            return FILE_TYPE_MARKDOWN;
        }

        // Unknown file type
        return FILE_TYPE_UNKNOWN;
    }

    /**
     * Display an image file in a dialog
     *
     * @param file Image file to display
     */
    private void showImageFile(File file) {
        try {
            String filePath = file.getAbsolutePath();
            logger.debug("Loading image from: " + filePath);

            // Add special handling for PNG files to ensure they display correctly
            Bitmap bitmap;
            if (filePath.toLowerCase().endsWith(".png")) {
                // For PNG files, we'll use a specific decoding approach
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888; // Support transparency
                bitmap = BitmapFactory.decodeFile(filePath, options);
                logger.debug("PNG image loaded, size: " +
                        (bitmap != null ? bitmap.getWidth() + "x" + bitmap.getHeight() : "null"));
            } else {
                // For other image types
                bitmap = BitmapFactory.decodeFile(filePath);
            }

            if (bitmap == null) {
                logger.debug("Failed to load image: bitmap is null");
                showUnknownFileError(file.getName());
                return;
            }

            // Create a dialog to display the image
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_image_preview);

            // Set dialog size to 90% of screen width
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);

            // Set the title and image
            TextView titleView = dialog.findViewById(R.id.image_title);
            titleView.setText(file.getName());

            ImageView imageView = dialog.findViewById(R.id.image_preview);
            imageView.setImageBitmap(bitmap);

            // Add close button
            dialog.findViewById(R.id.close_button).setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        } catch (Exception e) {
            logger.debug("Error displaying image", e);
            showUnknownFileError(file.getName());
        }
    }

    /**
     * Display a markdown or text file in a dialog
     *
     * @param file Text file to display
     */
    private void showMarkdownFile(File file) {
        BackgroundOps.execute(() -> {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString();
            } catch (IOException e) {
                logger.debug("Error reading text file", e);
                return null;
            }
        }, content -> {
            if (content == null) {
                showUnknownFileError(file.getName());
                return;
            }

            // Create a dialog to display the text
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_text_preview);

            // Set dialog size to 90% of screen width/height
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
            dialog.getWindow().setAttributes(layoutParams);

            // Set the title and text content
            TextView titleView = dialog.findViewById(R.id.text_title);
            titleView.setText(file.getName());

            TextView textView = dialog.findViewById(R.id.text_content);
            textView.setText(content);

            // Add close button
            dialog.findViewById(R.id.close_button).setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        });
    }

    /**
     * Show error message for unknown file types
     *
     * @param fileName Name of the file
     */
    private void showUnknownFileError(String fileName) {
        new AlertDialog.Builder(this)
                .setTitle("Unable to Open File")
                .setMessage("The file type of '" + fileName + "' is unknown or not supported.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!navigationHistory.isEmpty()) {
            currentDirectory = navigationHistory.pop();
            loadCurrentDirectory();
        } else {
            super.onBackPressed();
        }
    }
} 