package com.originb.inkwisenote2.modules.fileexplorer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.originb.inkwisenote2.R;

import java.util.ArrayList;
import java.util.List;

public class FileGroupAdapter extends RecyclerView.Adapter<FileGroupAdapter.FileGroupViewHolder> {

    private final List<FileGroup> fileGroups = new ArrayList<>(); // Initialize here
    private final Context context;
    private final OnFileGroupClickListener fileGroupClickListener;
    private final OnFileGroupDeleteListener fileGroupDeleteListener;

    public interface OnFileGroupClickListener {
        void onFileGroupClick(FileGroup fileGroup);
    }

    public interface OnFileGroupDeleteListener {
        void onFileGroupDelete(FileGroup fileGroup);
    }

    public FileGroupAdapter(Context context,
                            OnFileGroupClickListener fileGroupClickListener,
                            OnFileGroupDeleteListener fileGroupDeleteListener) {
        this.context = context;
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

        holder.groupIcon.setImageResource(fileGroup.isDirectory() ?
                R.drawable.ic_directory : R.drawable.ic_file);

        holder.itemView.setOnClickListener(v -> {
            if (fileGroupClickListener != null) fileGroupClickListener.onFileGroupClick(fileGroup);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (fileGroupDeleteListener != null) fileGroupDeleteListener.onFileGroupDelete(fileGroup);
        });
    }

    @Override
    public int getItemCount() {
        return fileGroups.size();
    }

    /**
     * Replaced notifyDataSetChanged with DiffUtil for performance and animations.
     */
    public void updateFileGroups(List<FileGroup> newFileGroups) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FileGroupDiffCallback(this.fileGroups, newFileGroups));
        this.fileGroups.clear();
        this.fileGroups.addAll(newFileGroups);
        diffResult.dispatchUpdatesTo(this);
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

    /**
     * Internal class to handle the comparison logic
     */
    private static class FileGroupDiffCallback extends DiffUtil.Callback {
        private final List<FileGroup> oldList;
        private final List<FileGroup> newList;

        public FileGroupDiffCallback(List<FileGroup> oldList, List<FileGroup> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            // Check if they are the same physical file/group (usually check ID or Path)
            return oldList.get(oldPos).getGroupName().equals(newList.get(newPos).getGroupName());
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            // Check if the contents (like file count or timestamp) changed
            FileGroup oldItem = oldList.get(oldPos);
            FileGroup newItem = newList.get(newPos);
            return oldItem.getFileCount() == newItem.getFileCount() &&
                    oldItem.getTimestamp().equals(newItem.getTimestamp());
        }
    }
}