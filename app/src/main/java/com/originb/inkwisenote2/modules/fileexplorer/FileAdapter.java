package com.originb.inkwisenote2.modules.fileexplorer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.originb.inkwisenote2.R;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private final List<FileItem> fileItems;
    private final Context context;
    private final OnFileClickListener fileClickListener;
    private final OnFileDeleteListener fileDeleteListener;

    public interface OnFileClickListener {
        void onFileClick(FileItem fileItem);
    }

    public interface OnFileDeleteListener {
        void onFileDelete(FileItem fileItem);
    }

    public FileAdapter(Context context, List<FileItem> fileItems, 
                      OnFileClickListener fileClickListener,
                      OnFileDeleteListener fileDeleteListener) {
        this.context = context;
        this.fileItems = fileItems;
        this.fileClickListener = fileClickListener;
        this.fileDeleteListener = fileDeleteListener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_directory_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem fileItem = fileItems.get(position);
        holder.fileName.setText(fileItem.getName());
        
        // Set appropriate icon based on whether it's a file or directory
        if (fileItem.isDirectory()) {
            holder.fileIcon.setImageResource(R.drawable.ic_directory);
        } else {
            holder.fileIcon.setImageResource(R.drawable.ic_file);
        }
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (fileClickListener != null) {
                fileClickListener.onFileClick(fileItem);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (fileDeleteListener != null) {
                fileDeleteListener.onFileDelete(fileItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileItems.size();
    }

    public void updateFiles(List<FileItem> newFiles) {
        fileItems.clear();
        fileItems.addAll(newFiles);
        notifyDataSetChanged();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView fileIcon;
        TextView fileName;
        ImageButton deleteButton;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIcon = itemView.findViewById(R.id.file_icon);
            fileName = itemView.findViewById(R.id.file_name);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
} 