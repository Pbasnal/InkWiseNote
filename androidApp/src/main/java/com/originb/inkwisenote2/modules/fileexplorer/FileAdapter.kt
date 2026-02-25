package com.originb.inkwisenote2.modules.fileexplorer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.modules.fileexplorer.FileAdapter.FileViewHolder

class FileAdapter(
    private val context: Context?, private val fileItems: MutableList<FileItem>,
    private val fileClickListener: OnFileClickListener?,
    private val fileDeleteListener: OnFileDeleteListener?
) : RecyclerView.Adapter<FileViewHolder>() {
    interface OnFileClickListener {
        fun onFileClick(fileItem: FileItem?)
    }

    interface OnFileDeleteListener {
        fun onFileDelete(fileItem: FileItem?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_directory_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileItem = fileItems[position]
        holder.fileName.text = fileItem.name

        // Set appropriate icon based on whether it's a file or directory
        if (fileItem.isDirectory) {
            holder.fileIcon.setImageResource(R.drawable.ic_directory)
        } else {
            holder.fileIcon.setImageResource(R.drawable.ic_file)
        }


        // Set click listeners
        holder.itemView.setOnClickListener(View.OnClickListener { v: View? ->
            if (fileClickListener != null) {
                fileClickListener.onFileClick(fileItem)
            }
        })

        holder.deleteButton.setOnClickListener(View.OnClickListener { v: View? ->
            if (fileDeleteListener != null) {
                fileDeleteListener.onFileDelete(fileItem)
            }
        })
    }

    override fun getItemCount(): Int {
        return fileItems.size
    }

    fun updateFiles(newFiles: MutableList<FileItem?>) {
        fileItems.clear()
        fileItems.addAll(newFiles.filterNotNull())
        notifyDataSetChanged()
    }

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fileIcon: ImageView
        var fileName: TextView
        var deleteButton: ImageButton

        init {
            fileIcon = itemView.findViewById<ImageView>(R.id.file_icon)
            fileName = itemView.findViewById<TextView>(R.id.file_name)
            deleteButton = itemView.findViewById<ImageButton>(R.id.delete_button)
        }
    }
}