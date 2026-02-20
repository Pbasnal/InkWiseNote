package com.originb.inkwisenote2.modules.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.originb.inkwisenote2.modules.admin.AdminUiState.DataList
import com.originb.inkwisenote2.modules.admin.AdminUiState.FilesState
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNotesDao
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.ocr.data.NoteTermFrequencyDao
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntitiesDao
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPagesDao
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBooksDao
import java.io.File
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer

class AdminViewModel(// DAOs
    private val termFreqDao: NoteTermFrequencyDao,
    private val ocrDao: NoteOcrTextsDao,
    private val atomicDao: AtomicNoteEntitiesDao,
    private val booksDao: SmartBooksDao,
    private val pagesDao: SmartBookPagesDao,
    private val handNotesDao: HandwrittenNotesDao
) : ViewModel() {
    // Observable Data
    private val _uiState = MutableLiveData<AdminUiState?>()
    var uiState: LiveData<AdminUiState?> = _uiState

    private val _toastMessage = MutableLiveData<String?>()
    var toastMessage: LiveData<String?> = _toastMessage

    private var currentDirectory: File? = null

    fun loadData(tabName: String, noteId: Long?, defaultDir: File?) {
        if (currentDirectory == null) currentDirectory = defaultDir

        BackgroundOps.execute<AdminUiState?>(Callable {
            when (tabName) {
                "Term Frequencies" -> return@execute DataList(termFreqDao.allTermFrequencies, tabName)
                "Note Text" -> return@execute DataList(ocrDao.allNoteText, tabName)
                "Atomic Notes" -> return@execute DataList(atomicDao.allAtomicNotes, tabName)
                "Smart Books" -> return@execute DataList(booksDao.allSmartBooks, tabName)
                "Smart Book Pages" -> return@execute DataList(pagesDao.allSmartBookPages, tabName)
                "Handwritten Notes" -> return@execute DataList(handNotesDao.allHandwrittenNotes, tabName)
                "Files" -> return@execute getFilesState(currentDirectory!!)
                else -> return@execute null
            }
        }, Consumer { result: AdminUiState? ->
            if (result != null) _uiState.setValue(result)
        })
    }

    private fun getFilesState(directory: File): FilesState {
        this.currentDirectory = directory
        val files = directory.listFiles()
        if (files != null) {
            Arrays.sort<File?>(files, Comparator { f1: File?, f2: File? ->
                if (f1!!.isDirectory() && !f2!!.isDirectory()) return@sort -1
                if (!f1.isDirectory() && f2!!.isDirectory()) return@sort 1
                f1.getName().compareTo(f2!!.getName(), ignoreCase = true)
            })
        }
        return FilesState(directory, if (files != null) Arrays.asList<File?>(*files) else null)
    }

    fun navigateToDir(directory: File?) {
        loadData("Files", 0L, directory)
    }

    fun deleteFile(file: File) {
        if (file.isDirectory() && file.listFiles() != null && file.listFiles().size > 0) {
            _toastMessage.setValue("Cannot delete non-empty directory")
            return
        }
        if (file.delete()) {
            _toastMessage.setValue("Deleted: " + file.getName())
            navigateToDir(currentDirectory)
        } else {
            _toastMessage.setValue("Failed to delete: " + file.getName())
        }
    }
}
