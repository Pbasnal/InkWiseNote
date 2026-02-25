package com.originb.inkwisenote2.modules.fileexplorer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.fileexplorer.FileGroupAdapter.FileGroupViewHolder

class FileGroupAdapter(
    private val context: Context?,
    private val fileGroupClickListener: OnFileGroupClickListener?,
    private val fileGroupDeleteListener: OnFileGroupDeleteListener?
) : RecyclerView.Adapter<FileGroupViewHolder>() {
    private val fileGroups: MutableList<FileGroup> = ArrayList<FileGroup>() // Initialize here

    interface OnFileGroupClickListener {
        fun onFileGroupClick(fileGroup: FileGroup?)
    }

    interface OnFileGroupDeleteListener {
        fun onFileGroupDelete(fileGroup: FileGroup?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileGroupViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_file_group, parent, false)
        return FileGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileGroupViewHolder, position: Int) {
        val fileGroup = fileGroups[position]

        holder.groupName.text = fileGroup.groupName ?: ""

        if (fileGroup.isGroup) {
            holder.timestamp.text = fileGroup.timestamp ?: ""
            holder.timestamp.visibility = View.VISIBLE
            holder.fileCount.text = fileGroup.fileCount.toString()
            holder.fileCount.visibility = View.VISIBLE
        } else {
            holder.timestamp.visibility = View.GONE
            holder.fileCount.visibility = View.GONE
        }

        holder.groupIcon.setImageResource(if (fileGroup.isDirectory) R.drawable.ic_directory else R.drawable.ic_file)

        holder.itemView.setOnClickListener {
            fileGroupClickListener?.onFileGroupClick(fileGroup)
        }

        holder.deleteButton.setOnClickListener {
            fileGroupDeleteListener?.onFileGroupDelete(fileGroup)
        }
    }

    override fun getItemCount(): Int {
        return fileGroups.size
    }

    /**
     * Replaced notifyDataSetChanged with DiffUtil for performance and animations.
     */
    fun updateFileGroups(newFileGroups: MutableList<FileGroup>) {
        val diffResult = DiffUtil.calculateDiff(FileGroupDiffCallback(this.fileGroups, newFileGroups))
        this.fileGroups.clear()
        this.fileGroups.addAll(newFileGroups)
        diffResult.dispatchUpdatesTo(this)
    }

    class FileGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var groupIcon: ImageView
        var groupName: TextView
        var timestamp: TextView
        var fileCount: TextView
        var deleteButton: ImageButton

        init {
            groupIcon = itemView.findViewById<ImageView>(R.id.group_icon)
            groupName = itemView.findViewById<TextView>(R.id.group_name)
            timestamp = itemView.findViewById<TextView>(R.id.timestamp)
            fileCount = itemView.findViewById<TextView>(R.id.file_count)
            deleteButton = itemView.findViewById<ImageButton>(R.id.delete_button)
        }
    }

    /**
     * Internal class to handle the comparison logic
     */
    private class FileGroupDiffCallback(
        private val oldList: MutableList<FileGroup>,
        private val newList: MutableList<FileGroup>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return (oldList[oldPos].groupName ?: "") == (newList[newPos].groupName ?: "")
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            val oldItem = oldList[oldPos]
            val newItem = newList[newPos]
            return oldItem.fileCount == newItem.fileCount &&
                    oldItem.timestamp == newItem.timestamp
        }
    }
}