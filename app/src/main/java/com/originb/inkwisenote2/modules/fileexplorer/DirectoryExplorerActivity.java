package com.originb.inkwisenote2.modules.fileexplorer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
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
import java.util.List;

public class DirectoryExplorerActivity extends AppCompatActivity
        implements FileGroupAdapter.OnFileGroupClickListener, FileGroupAdapter.OnFileGroupDeleteListener {

    private DirectoryExplorerViewModel viewModel;
    private RecyclerView recyclerView;
    private FileGroupAdapter fileGroupAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private Toolbar toolbar;

    private final Logger logger = new Logger("DirectoryExplorerActivity");

    // File type constants
    private static final int FILE_TYPE_IMAGE = 1;
    private static final int FILE_TYPE_MARKDOWN = 2;
    private static final int FILE_TYPE_UNKNOWN = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_explorer);

        // 1. Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DirectoryExplorerViewModel.class);

        // 2. Initialize Views
        initViews();

        // 3. Setup Observers
        setupObservers();

        // 4. Initial Load
        viewModel.init(getFilesDir());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.files_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        emptyView = findViewById(R.id.empty_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.loadCurrentDirectory());
        setupRecyclerView();
    }

    private void setupObservers() {
        // Observe file data
        viewModel.fileGroups.observe(this, groups -> {
            fileGroupAdapter.updateFileGroups(groups);
            if (groups.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        // Observe loading state
        viewModel.isRefreshing.observe(this, isRefreshing ->
                swipeRefreshLayout.setRefreshing(isRefreshing));

        // Observe toolbar title
        viewModel.toolbarTitle.observe(this, title -> {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
        });

        // Observe toast messages from ViewModel
        viewModel.toastMessage.observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        // Calculate columns
        int numColumns = Math.max(2, (int) (dpWidth / 120));

        // Setup LayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(this, numColumns);
        recyclerView.setLayoutManager(layoutManager);

        // Add Spacing (8dp translated to pixels)
        int spacingInPixels = (int) (8 * displayMetrics.density);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(numColumns, spacingInPixels, true));

        // Initialize Adapter with the new clean constructor
        fileGroupAdapter = new FileGroupAdapter(this, this, this);
        recyclerView.setAdapter(fileGroupAdapter);
    }

    @Override
    public void onFileGroupClick(FileGroup fileGroup) {
        if (fileGroup.isDirectory()) {
            viewModel.navigateInto(fileGroup.getPrimaryFile().getFile());
        } else if (fileGroup.isGroup()) {
            showFileGroupContents(fileGroup);
        } else {
            FileItem singleFile = fileGroup.getPrimaryFile();
            if (singleFile != null) {
                showFileByType(singleFile);
            }
        }
    }

    @Override
    public void onFileGroupDelete(FileGroup fileGroup) {
        String message;
        if (fileGroup.isGroup()) {
            message = "Delete all " + fileGroup.getFileCount() + " files in '" + fileGroup.getGroupName() + "'?";
        } else {
            FileItem item = fileGroup.getPrimaryFile();
            if (item == null) return;
            message = "Delete " + (item.isDirectory() ? "directory" : "file") + " '" + item.getName() + "'?";
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage(message)
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteFileGroup(fileGroup))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- File Preview Logic (View Layer) ---

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

    private void showImageFile(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (file.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            }
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            if (bitmap == null) {
                showUnknownFileError(file.getName());
                return;
            }

            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_image_preview);

            // Adjust dialog width
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setAttributes(lp);

            ((TextView) dialog.findViewById(R.id.image_title)).setText(file.getName());
            ((ImageView) dialog.findViewById(R.id.image_preview)).setImageBitmap(bitmap);
            dialog.findViewById(R.id.close_button).setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } catch (Exception e) {
            showUnknownFileError(file.getName());
        }
    }

    private void showMarkdownFile(File file) {
        // We use BackgroundOps here because reading the file is a temporary UI-only task
        BackgroundOps.execute(() -> {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) content.append(line).append("\n");
                return content.toString();
            } catch (IOException e) { return null; }
        }, content -> {
            if (content == null) {
                showUnknownFileError(file.getName());
                return;
            }
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_text_preview);

            // Adjust dialog size
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
            dialog.getWindow().setAttributes(lp);

            ((TextView) dialog.findViewById(R.id.text_title)).setText(file.getName());
            ((TextView) dialog.findViewById(R.id.text_content)).setText(content);
            dialog.findViewById(R.id.close_button).setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        });
    }

    private void showFileGroupContents(FileGroup fileGroup) {
        List<FileItem> files = fileGroup.getFiles();
        String[] names = files.stream().map(FileItem::getName).toArray(String[]::new);

        new AlertDialog.Builder(this)
                .setTitle("Files in " + fileGroup.getGroupName())
                .setItems(names, (d, which) -> showFileByType(files.get(which)))
                .setPositiveButton("Close", null)
                .show();
    }

    private int getFileType(String fileName) {
        if (fileName == null) return FILE_TYPE_UNKNOWN;
        String name = fileName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")) return FILE_TYPE_IMAGE;
        if (name.endsWith(".md") || name.endsWith(".txt") || name.endsWith(".pt")) return FILE_TYPE_MARKDOWN;
        return FILE_TYPE_UNKNOWN;
    }

    private void showUnknownFileError(String fileName) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Cannot open '" + fileName + "'")
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
        if (!viewModel.navigateBack()) {
            super.onBackPressed();
        }
    }
}