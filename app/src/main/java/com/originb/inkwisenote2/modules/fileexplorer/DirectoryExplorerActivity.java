package com.originb.inkwisenote2.modules.fileexplorer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;
import java.util.Stack;

public class DirectoryExplorerActivity extends AppCompatActivity 
        implements FileAdapter.OnFileClickListener, FileAdapter.OnFileDeleteListener {

    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private Toolbar toolbar;
    
    private File currentDirectory;
    private final Stack<File> navigationHistory = new Stack<>();
    private final Logger logger = new Logger("DirectoryExplorerActivity");
    
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
        fileAdapter = new FileAdapter(this, new ArrayList<>(), this, this);
        recyclerView.setAdapter(fileAdapter);
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
                
                // Sort: directories first, then files, both alphabetically
                Collections.sort(fileItems, (o1, o2) -> {
                    if (o1.isDirectory() && !o2.isDirectory()) {
                        return -1;
                    } else if (!o1.isDirectory() && o2.isDirectory()) {
                        return 1;
                    } else {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });
                
                return fileItems;
            }
            
            return fileItems;
        }, result -> {
            swipeRefreshLayout.setRefreshing(false);
            fileAdapter.updateFiles(result);
            
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
    
    @Override
    public void onFileClick(FileItem fileItem) {
        if (fileItem.isDirectory()) {
            // Navigate into directory
            navigationHistory.push(currentDirectory);
            currentDirectory = fileItem.getFile();
            loadCurrentDirectory();
        } else {
            // Show file details or open it
            Toast.makeText(this, "File: " + fileItem.getName(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onFileDelete(FileItem fileItem) {
        new AlertDialog.Builder(this)
                .setTitle("Delete " + (fileItem.isDirectory() ? "Directory" : "File"))
                .setMessage("Are you sure you want to delete " + fileItem.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteFile(fileItem))
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void deleteFile(FileItem fileItem) {
        BackgroundOps.execute(() -> {
            boolean success;
            if (fileItem.isDirectory()) {
                success = deleteDirectory(fileItem.getFile());
            } else {
                success = fileItem.getFile().delete();
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