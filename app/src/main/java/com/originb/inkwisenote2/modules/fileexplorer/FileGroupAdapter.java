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

public class FileGroupAdapter extends RecyclerView.Adapter<FileGroupAdapter.FileGroupViewHolder> {

    private final List<FileGroup> fileGroups;
    private final Context context;
    private final OnFileGroupClickListener fileGroupClickListener;
    private final OnFileGroupDeleteListener fileGroupDeleteListener;

    public interface OnFileGroupClickListener {
        void onFileGroupClick(FileGroup fileGroup);
    }

    public interface OnFileGroupDeleteListener {
        void onFileGroupDelete(FileGroup fileGroup);
    }

    public FileGroupAdapter(Context context, List<FileGroup> fileGroups, 
                      OnFileGroupClickListener fileGroupClickListener,
                      OnFileGroupDeleteListener fileGroupDeleteListener) {
        this.context = context;
        this.fileGroups = fileGroups;
        this.fileGroupClickListener = fileGroupClickListener;
        this.fileGroupDeleteListener = fileGroupDeleteListener;
    }

    @NonNull
    @Override
    public FileGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file_group, parent, false);
        return new FileGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileGroupViewHolder holder, int position) {
        FileGroup fileGroup = fileGroups.get(position);
        
        // Set group name and timestamp
        holder.groupName.setText(fileGroup.getGroupName());
        
        if (fileGroup.isGroup()) {
            holder.timestamp.setText(fileGroup.getTimestamp());
            holder.timestamp.setVisibility(View.VISIBLE);
            holder.fileCount.setText(String.valueOf(fileGroup.getFileCount()));
            holder.fileCount.setVisibility(View.VISIBLE);
        } else {
            holder.timestamp.setVisibility(View.GONE);
            holder.fileCount.setVisibility(View.GONE);
        }
        
        // Set appropriate icon based on whether it's a directory or file
        if (fileGroup.isDirectory()) {
            holder.groupIcon.setImageResource(R.drawable.ic_directory);
        } else {
            holder.groupIcon.setImageResource(R.drawable.ic_file);
        }
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (fileGroupClickListener != null) {
                fileGroupClickListener.onFileGroupClick(fileGroup);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (fileGroupDeleteListener != null) {
                fileGroupDeleteListener.onFileGroupDelete(fileGroup);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileGroups.size();
    }

    public void updateFileGroups(List<FileGroup> newFileGroups) {
        fileGroups.clear();
        fileGroups.addAll(newFileGroups);
        notifyDataSetChanged();
    }

    static class FileGroupViewHolder extends RecyclerView.ViewHolder {
        ImageView groupIcon;
        TextView groupName;
        TextView timestamp;
        TextView fileCount;
        ImageButton deleteButton;

        public FileGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupIcon = itemView.findViewById(R.id.group_icon);
            groupName = itemView.findViewById(R.id.group_name);
            timestamp = itemView.findViewById(R.id.timestamp);
            fileCount = itemView.findViewById(R.id.file_count);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
} 