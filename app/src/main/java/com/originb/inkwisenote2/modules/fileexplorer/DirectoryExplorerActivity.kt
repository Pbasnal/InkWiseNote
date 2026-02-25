package com.originb.inkwisenote2.modules.fileexplorer

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.fileexplorer.FileGroupAdapter.OnFileGroupClickListener
import com.originb.inkwisenote2.modules.fileexplorer.FileGroupAdapter.OnFileGroupDeleteListener
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.concurrent.Callable
import java.util.function.Consumer
import kotlin.math.max

class DirectoryExplorerActivity : AppCompatActivity(), OnFileGroupClickListener, OnFileGroupDeleteListener {
    private var viewModel: DirectoryExplorerViewModel? = null
    private var recyclerView: RecyclerView? = null
    private var fileGroupAdapter: FileGroupAdapter? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var emptyView: TextView? = null
    private var toolbar: Toolbar? = null

    private val logger = Logger("DirectoryExplorerActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directory_explorer)

        // 1. Initialize ViewModel
        viewModel = ViewModelProvider(this).get<DirectoryExplorerViewModel>(DirectoryExplorerViewModel::class.java)

        // 2. Initialize Views
        initViews()

        // 3. Setup Observers
        setupObservers()

        // 4. Initial Load
        viewModel!!.init(getFilesDir())
    }

    private fun initViews() {
        recyclerView = findViewById<RecyclerView>(R.id.files_recycler_view)
        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        emptyView = findViewById<TextView>(R.id.empty_view)
        toolbar = findViewById<Toolbar?>(R.id.toolbar)

        setSupportActionBar(toolbar)
        if (getSupportActionBar() != null) {
            getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
            getSupportActionBar()!!.setDisplayShowHomeEnabled(true)
        }

        swipeRefreshLayout!!.setOnRefreshListener(OnRefreshListener { viewModel!!.loadCurrentDirectory() })
        setupRecyclerView()
    }

    private fun setupObservers() {
        // Observe file data
        viewModel!!.fileGroups.observe(this, Observer { groups: MutableList<FileGroup?>? ->
            val list = groups?.filterNotNull()?.toMutableList() ?: mutableListOf()
            fileGroupAdapter!!.updateFileGroups(list)
            if (list.isEmpty()) {
                emptyView!!.setVisibility(View.VISIBLE)
                recyclerView!!.setVisibility(View.GONE)
            } else {
                emptyView!!.setVisibility(View.GONE)
                recyclerView!!.setVisibility(View.VISIBLE)
            }
        })

        // Observe loading state
        viewModel!!.isRefreshing.observe(
            this,
            Observer { isRefreshing: Boolean? -> swipeRefreshLayout!!.setRefreshing(isRefreshing!!) })

        // Observe toolbar title
        viewModel!!.toolbarTitle.observe(this, Observer { title: String? ->
            if (getSupportActionBar() != null) {
                getSupportActionBar()!!.setTitle(title)
            }
        })

        // Observe toast messages from ViewModel
        viewModel!!.toastMessage.observe(this, Observer { message: String? ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView() {
        val displayMetrics = getResources().getDisplayMetrics()
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density

        // Calculate columns
        val numColumns = max(2, (dpWidth / 120).toInt())

        // Setup LayoutManager
        val layoutManager = GridLayoutManager(this, numColumns)
        recyclerView!!.setLayoutManager(layoutManager)

        // Add Spacing (8dp translated to pixels)
        val spacingInPixels = (8 * displayMetrics.density).toInt()
        recyclerView!!.addItemDecoration(GridSpacingItemDecoration(numColumns, spacingInPixels, true))

        // Initialize Adapter with the new clean constructor
        fileGroupAdapter = FileGroupAdapter(this, this, this)
        recyclerView!!.setAdapter(fileGroupAdapter)
    }

    override fun onFileGroupClick(fileGroup: FileGroup?) {
        if (fileGroup == null) return
        if (fileGroup.isDirectory) {
            fileGroup.primaryFile?.let { viewModel!!.navigateInto(it.file) }
        } else if (fileGroup.isGroup) {
            showFileGroupContents(fileGroup)
        } else {
            fileGroup.primaryFile?.let { showFileByType(it) }
        }
    }

    override fun onFileGroupDelete(fileGroup: FileGroup?) {
        if (fileGroup == null) return
        val message: String?
        if (fileGroup.isGroup) {
            message = "Delete all " + fileGroup.fileCount + " files in '" + fileGroup.groupName + "'?"
        } else {
            val item = fileGroup.primaryFile
            if (item == null) return
            message = "Delete " + (if (item.isDirectory) "directory" else "file") + " '" + item.name + "'?"
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage(message)
            .setPositiveButton(
                "Delete",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    viewModel!!.deleteFileGroup(fileGroup)
                })
            .setNegativeButton("Cancel", null)
            .show()
    }

    // --- File Preview Logic (View Layer) ---
    private fun showFileByType(fileItem: FileItem) {
        val fileType = getFileType(fileItem.name)
        when (fileType) {
            FILE_TYPE_IMAGE -> showImageFile(fileItem.file)
            FILE_TYPE_MARKDOWN -> showMarkdownFile(fileItem.file)
            else -> showUnknownFileError(fileItem.name)
        }
    }

    private fun showImageFile(file: File) {
        try {
            val options = BitmapFactory.Options()
            if (file.name.lowercase().endsWith(".png")) {
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options)

            if (bitmap == null) {
                showUnknownFileError(file.name)
                return
            }

            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_image_preview)

            // Adjust dialog width
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.getWindow()!!.getAttributes())
            lp.width = (getResources().getDisplayMetrics().widthPixels * 0.9).toInt()
            dialog.getWindow()!!.setAttributes(lp)

            (dialog.findViewById<View?>(R.id.image_title) as TextView).setText(file.name)
            (dialog.findViewById<View?>(R.id.image_preview) as ImageView).setImageBitmap(bitmap)
            dialog.findViewById<View?>(R.id.close_button)
                .setOnClickListener(View.OnClickListener { v: View? -> dialog.dismiss() })
            dialog.show()
        } catch (e: Exception) {
            showUnknownFileError(file.name)
        }
    }

    private fun showMarkdownFile(file: File) {
        BackgroundOps.execute(Callable {
            val content = StringBuilder()
            try {
                BufferedReader(FileReader(file)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) content.append(line).append("\n")
                }
                content.toString()
            } catch (e: IOException) {
                null
            }
        }, Consumer { content: String? ->
            if (content == null) {
                showUnknownFileError(file.name)
                return@Consumer
            }
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_text_preview)

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window!!.attributes)
            lp.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            lp.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
            dialog.window!!.attributes = lp

            (dialog.findViewById<View?>(R.id.text_title) as TextView).text = file.name
            (dialog.findViewById<View?>(R.id.text_content) as TextView).text = content
            dialog.findViewById<View?>(R.id.close_button).setOnClickListener { dialog.dismiss() }
            dialog.show()
        })
    }

    private fun showFileGroupContents(fileGroup: FileGroup) {
        val files = fileGroup.files
        val names = files.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Files in " + fileGroup.groupName)
            .setItems(names) { _: DialogInterface?, which: Int -> showFileByType(files[which]) }
            .setPositiveButton("Close", null)
            .show()
    }

    private fun getFileType(fileName: String?): Int {
        if (fileName == null) return FILE_TYPE_UNKNOWN
        val name = fileName.lowercase()
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")) return FILE_TYPE_IMAGE
        if (name.endsWith(".md") || name.endsWith(".txt") || name.endsWith(".pt")) return FILE_TYPE_MARKDOWN
        return FILE_TYPE_UNKNOWN
    }

    private fun showUnknownFileError(fileName: String?) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("Cannot open '" + fileName + "'")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!viewModel!!.navigateBack()) {
            super.onBackPressed()
        }
    }

    companion object {
        // File type constants
        private const val FILE_TYPE_IMAGE = 1
        private const val FILE_TYPE_MARKDOWN = 2
        private const val FILE_TYPE_UNKNOWN = 0
    }
}