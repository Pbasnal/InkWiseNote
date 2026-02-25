package com.originb.inkwisenote2.modules.smartnotes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.common.isNotEmpty
import com.originb.inkwisenote2.common.isNullOrWhitespace
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.backgroundjobs.Events.*
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain.Companion.constructAtomicNote
import com.originb.inkwisenote2.modules.repositories.SmartNotebook
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smartnotes.data.*
import com.originb.inkwisenote2.modules.textnote.data.TextNoteEntity
import com.originb.inkwisenote2.modules.textnote.data.TextNotesDao
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer

class SmartNotebookViewModel(
    application: Application,
    private val smartNotebookRepository: SmartNotebookRepository,
    private val atomicNotesDomain: AtomicNotesDomain,
    private val handwrittenNoteRepository: HandwrittenNoteRepository,
    private val textNotesDao: TextNotesDao
) : AndroidViewModel(application) {
    private val logger = Logger("SmartNotebookViewModel")

    // Lock for synchronizing page navigation
    private val pageSwitchLock = Any()

    private var workingNotePath: String? = null
    private val currentPageIndexLive = MutableLiveData<Int?>(0)
    private val smartNotebookUpdate = MutableLiveData<SmartNotebookUpdate?>()
    private val notebookTitle = MutableLiveData<String?>("")
    private val navigationDataLive = MutableLiveData<NotebookNavigationData?>(NotebookNavigationData())
    private val createdTimeMillis = MutableLiveData<Long?>()

    fun onNotebookIsInDb(ifNotebookIsSaved: Consumer<Boolean?>) {
        val update = getSmartNotebookUpdate().value ?: return
        val notebook = update.smartNotebook

        BackgroundOps.execute(
            Callable { smartNotebookRepository.bookExists(notebook) },
            ifNotebookIsSaved
        )
    }

    fun setNotebookTitle(title: String?) {
        val notebookUpdate = smartNotebookUpdate.value ?: return
        notebookUpdate.smartNotebook.smartBook.title = title
        notebookUpdate.notbookUpdateType = SmartNotebookUpdateType.NOTEBOOK_TITLE_UPDATED
        smartNotebookUpdate.value = notebookUpdate
    }

    class SmartNotebookUpdate(var smartNotebook: SmartNotebook, var atomicNote: AtomicNoteEntity?) {
        var notbookUpdateType: SmartNotebookUpdateType = SmartNotebookUpdateType.NOTE_UPDATE
        var indexOfUpdatedNote: Int = -1

        companion object {
            fun fromNotebook(smartNotebook: SmartNotebook): SmartNotebookUpdate {
                return SmartNotebookUpdate(smartNotebook, null)
            }

            fun noteDeleted(smartNotebook: SmartNotebook, atomicNote: AtomicNoteEntity?): SmartNotebookUpdate {
                val smartNotebookUpdate = SmartNotebookUpdate(smartNotebook, atomicNote)
                smartNotebookUpdate.notbookUpdateType = SmartNotebookUpdateType.NOTE_DELETED
                return smartNotebookUpdate
            }

            fun fromNoteAndBook(
                updatedNotebook: SmartNotebook,
                indexOfUpdatedNote: Int
            ): SmartNotebookUpdate {
                val smartNotebookUpdate = SmartNotebookUpdate(updatedNotebook, null)
                smartNotebookUpdate.smartNotebook = updatedNotebook
                smartNotebookUpdate.indexOfUpdatedNote = indexOfUpdatedNote
                return smartNotebookUpdate
            }

            fun notebookDeleted(deletedNotebook: SmartNotebook): SmartNotebookUpdate {
                val smartNotebookUpdate = SmartNotebookUpdate(deletedNotebook, null)
                smartNotebookUpdate.notbookUpdateType = SmartNotebookUpdateType.NOTEBOOK_DELETED
                return smartNotebookUpdate
            }
        }
    }

    init {
        EventBus.getDefault().register(this)
    }

    fun getCurrentPageIndexLive(): LiveData<Int?> {
        return currentPageIndexLive
    }

    fun getSmartNotebookUpdate(): LiveData<SmartNotebookUpdate?> {
        return smartNotebookUpdate
    }

    fun getNotebookTitle(): LiveData<String?> {
        return notebookTitle
    }

    fun getNavigationDataLive(): LiveData<NotebookNavigationData?> {
        return navigationDataLive
    }

    fun getCreatedTimeMillis(): LiveData<Long?> {
        return createdTimeMillis
    }

    fun loadSmartNotebook(bookId: Long?, workingPath: String?, bookTitle: String?, noteIdsString: String?) {
        this.workingNotePath = workingPath
        if (!isNullOrWhitespace(bookTitle) && workingPath != null) {
            this.workingNotePath = Paths.get(workingPath, bookTitle).toString()
        }

        BackgroundOps.executeOpt(
            { getSmartNotebook(bookId!!, bookTitle, noteIdsString!!) },
            { notebook ->
                smartNotebookUpdate.value = SmartNotebookUpdate.fromNotebook(notebook!!)
                notebookTitle.value = notebook. smartBook.title
                createdTimeMillis.value = notebook.smartBook.createdTimeMillis
                updatePageNumberText()
                if (bookTitle == null && workingPath != null) {
                    val bookTitleFromDb =
                        notebook.smartBook.title ?: notebook.smartBook.createdTimeMillis.toString()
                    workingNotePath = Paths.get(workingPath, bookTitleFromDb).toString()
                }
            }
        )
    }

    fun navigateToPageIndex(pageIndex: Int) {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook
        if (pageIndex < notebook.atomicNotes.size) {
            currentPageIndexLive.value = pageIndex
            updatePageNumberText()
        }
    }

    fun navigateToNextPage() {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook

        val currentIndex: Int = currentPageIndexLive.getValue()!!
        val nextIndex = currentIndex + 1
        if (nextIndex < notebook.atomicNotes.size) {
            currentPageIndexLive.value = nextIndex
            updatePageNumberText()
        }
    }

    fun navigateToPreviousPage() {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook

        val currentIndex: Int = currentPageIndexLive.getValue()!!
        val prevIndex = currentIndex - 1

        if (prevIndex >= 0) {
            currentPageIndexLive.value = prevIndex
            updatePageNumberText()
        }
    }

    fun addNewPage() {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook

        val currentIndex: Int = currentPageIndexLive.getValue()!!
        val indexToInsertAt = currentIndex + 1

        BackgroundOps.execute(
            {
                val newAtomicNote = atomicNotesDomain.saveAtomicNote(
                    constructAtomicNote(
                        "",
                        workingNotePath!!,
                        NoteType.NOT_SET
                    )
                )
                val newSmartPage = smartNotebookRepository.newSmartBookPage(
                    notebook.smartBook, newAtomicNote, indexToInsertAt
                )
                notebook.insertAtomicNoteAndPage(indexToInsertAt, newAtomicNote, newSmartPage)
                notebook
            },
            { updatedNotebook: SmartNotebook? ->
                if (updatedNotebook != null) {
                    smartNotebookUpdate.value = SmartNotebookUpdate.fromNoteAndBook(
                        updatedNotebook,
                        indexToInsertAt
                    )
                    currentPageIndexLive.value = indexToInsertAt
                    updatePageNumberText()
                }
            }
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotebookDelete(notebookDeleted: NotebookDeleted) {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook
        val deletedNotebook = notebookDeleted.smartNotebook
        if (deletedNotebook.smartBook.bookId != notebook.smartBook.bookId) return
        deleteNotebookFolder(notebook.smartBook.title)
        smartNotebookUpdate.value = SmartNotebookUpdate.notebookDeleted(deletedNotebook)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNoteDelete(noteDeleted: NoteDeleted) {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook

        val noteId = noteDeleted.atomicNote.noteId
        notebook.removeNote(noteId)

        if (notebook.atomicNotes.isEmpty()) {
            BackgroundOps.execute(
                { smartNotebookRepository.deleteSmartNotebook(notebook) },
                Runnable { deleteNotebookFolder(notebook.smartBook.title) }
            )
            return
        }

        smartNotebookUpdate.value = SmartNotebookUpdate.noteDeleted(notebook, noteDeleted.atomicNote)

        val currentPageIndex: Int = currentPageIndexLive.getValue()!!
        if (currentPageIndex == notebook.atomicNotes.size) {
            currentPageIndexLive.value = currentPageIndex - 1
        }

        updatePageNumberText()
    }

    private fun deleteNotebookFolder(smartNotebookTitle: String?) {
        val titleStr: String = smartNotebookTitle ?: ""
        val basePath: String = workingNotePath ?: ""
        val notebookPath = Paths.get(basePath, titleStr)
        try {
            Files.walk(notebookPath)
                .sorted(Comparator.reverseOrder<Path>()) // Delete children before parent
                .forEach { path: Path ->
                    try {
                        Files.delete(path)
                    } catch (e: IOException) {
                        System.err.println("Unable to delete: " + path + " : " + e.message)
                    }
                }
            Files.delete(notebookPath)
        } catch (e: Exception) {
            println("Failed to delete the file: " + e.message)
        }
    }

    fun saveNoteInCorrectFolder(
        atomicNote: AtomicNoteEntity,
        newNotebookPath: String,
        noteHolderData: NoteHolderData?
    ) {
        // Store the old path to move files
        val oldFilePath = atomicNote.filepath

        // Update the filepath
        atomicNote.filepath = newNotebookPath

        // Ensure directory exists
        val directory = File(newNotebookPath)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Move existing files if they exist
        moveFileIfExists(oldFilePath, newNotebookPath, (atomicNote.filename ?: "") + ".png")
        moveFileIfExists(oldFilePath, newNotebookPath, (atomicNote.filename ?: "") + "-t.png")
        moveFileIfExists(oldFilePath, newNotebookPath, (atomicNote.filename ?: "") + ".pt")
        moveFileIfExists(oldFilePath, newNotebookPath, (atomicNote.filename ?: "") + ".md")

        BackgroundOps.execute(Runnable {
            atomicNotesDomain.updateAtomicNote(atomicNote)
            saveCurrentNote(atomicNote, noteHolderData)
        })
    }

    /**
     * Moves a file from source to destination if it exists
     */
    private fun moveFileIfExists(sourcePath: String?, destPath: String?, filename: String) {
        try {
            val src = sourcePath ?: return
            val dest = destPath ?: return
            val sourceFile = File(src, filename)
            if (sourceFile.exists()) {
                val destFile = File(dest, filename)
                if (!sourceFile.renameTo(destFile)) {
                    logger.error("Failed to move file: $filename")
                }
            }
        } catch (e: Exception) {
            logger.exception("Error moving file: $filename", e)
        }
    }

    fun updateTitle(updatedTitle: String): Boolean {
        if (smartNotebookUpdate.getValue() == null) return false
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook

        if (isNotEmpty(updatedTitle)) {
            notebook.smartBook.title = updatedTitle
            notebookTitle.value = updatedTitle
            return true
        }
        return false
    }

    fun renameNotebookFolderName(newNotebookPath: String, oldNotebookTitle: String?): Boolean {
        val newFolder = File(newNotebookPath)
        var updateNotesFolderName = true
        val pathStr = (workingNotePath ?: "").plus("/").plus(oldNotebookTitle ?: "")
        val oldFolder = File(pathStr)
        updateNotesFolderName = oldFolder.renameTo(newFolder)
        if (!newFolder.exists()) {
            updateNotesFolderName = newFolder.mkdirs()
        }
        return updateNotesFolderName
    }


    fun saveCurrentSmartNotebook() {
        // This method is called when the activity is paused or stopped
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook
        val title = notebookTitle.getValue()
        if (title == null) return

        notebook.smartBook.title = title
        if (notebook.smartBook.bookId > -1) {
            BackgroundOps.execute { smartNotebookRepository.updateNotebook(notebook, getApplication()) }
        } else {
            BackgroundOps.execute { smartNotebookRepository.saveSmartNotebook(notebook, getApplication()) }
        }
    }

    fun saveCurrentNote(atomicNote: AtomicNoteEntity?, noteData: NoteHolderData?) {
        if (atomicNote == null || noteData == null) {
            logger.error("Attempted to save null note or note data")
            return
        }

        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook

        synchronized(pageSwitchLock) {
            try {
                saveCurrentNoteAsPerType(atomicNote, noteData, notebook)
            } catch (e: Exception) {
                logger.exception("Error saving note", e)
            }
        }
    }

    fun saveCurrentNoteAsPerType(atomicNote: AtomicNoteEntity, noteData: NoteHolderData, notebook: SmartNotebook) {
        when (noteData.noteType) {
            NoteType.HANDWRITTEN_PNG -> handwrittenNoteRepository.saveHandwrittenNotes(
                notebook.smartBook.bookId,
                atomicNote,
                noteData.bitmap!!,
                noteData.pageTemplate,
                noteData.strokes?.filterNotNull()?.toMutableList(),
                getApplication()
            )

            NoteType.TEXT_NOTE -> {
                // Save to database
                var textNoteEntity = textNotesDao.getTextNoteForNote(atomicNote.noteId)
                textNoteEntity.noteText = noteData.noteText
                textNotesDao.updateTextNote(textNoteEntity)

                // Save to markdown file
                saveNoteToMarkdownFile(atomicNote, noteData.noteText)

                // Post event
                EventBus.getDefault().post(
                    TextNoteSaved(
                        notebook.smartBook.bookId,
                        atomicNote,
                        getApplication()
                    )
                )
            }

            else -> {}
        }
    }

    /**
     * Save note text to a markdown file
     */
    private fun saveNoteToMarkdownFile(note: AtomicNoteEntity, noteText: String?) {
        BackgroundOps.execute(
            {
                val filename = (note.filename ?: "").toString() + ".md"
                var notebookDir: String? = workingNotePath
                if (isNullOrWhitespace(notebookDir)) {
                    notebookDir = note.filepath
                }
                val dirStr = (notebookDir ?: "").toString()
                val file = File(dirStr, filename)
                try {
                    val content = (noteText ?: "").toString()
                    FileWriter(file).use { it.write(content) }
                    true
                } catch (e: IOException) {
                    logger.exception("Error saving markdown file", e)
                    false
                }
            },
            { success ->
                if (success != true) {
                    logger.error("Failed to save markdown file for note: " + note.noteId)
                }
            }
        )
    }

    val currentNote: AtomicNoteEntity?
        get() {
            val update = smartNotebookUpdate.value ?: return null
            val notebook = update.smartNotebook
            val currentNoteIndex = currentPageIndexLive.value ?: return null
            return notebook.atomicNotes[currentNoteIndex]
        }

    private fun getSmartNotebook(
        bookIdToOpen: Long,
        bookTitle: String?,
        noteIdsString: String
    ): SmartNotebook? {
        if (bookIdToOpen != -1L) {
            return smartNotebookRepository.getSmartNotebooks(bookIdToOpen)
        } else if (!noteIdsString.isEmpty()) {
            val noteIdsList = noteIdsString.split(",").dropLastWhile { it.isEmpty() }
            val noteIdsSet: MutableSet<Long> = mutableSetOf()
            noteIdsList.mapNotNull { it.toLongOrNull() }
                .forEach { noteIdsSet.add(it) }

            val smartNotebooks = smartNotebookRepository.getSmartNotebooksForNoteIds(noteIdsSet)
            if (smartNotebooks.size == 1) { // if all the notes belong to an existing notebook
                val notebookWithAllNotes = smartNotebooks.first()
                val noteIdsInNotebook: MutableSet<Long?> = notebookWithAllNotes.atomicNotes
                    .map { obj: AtomicNoteEntity? -> obj?.noteId }
                    .toMutableSet()

                noteIdsInNotebook.removeAll(noteIdsSet)
                if (!noteIdsInNotebook.isEmpty()) return notebookWithAllNotes
            }

            return smartNotebookRepository.getVirtualSmartNotebooks(bookTitle, noteIdsSet)
        }

        return smartNotebookRepository.initializeNewSmartNotebook(
            "", workingNotePath!!, NoteType.NOT_SET
        )
    }

    private fun updatePageNumberText() {
        val notebook = smartNotebookUpdate.value?.smartNotebook ?: return
        val index = currentPageIndexLive.value ?: return

        val text = (index + 1).toString() + "/" + notebook.atomicNotes.size

        val navigationData = navigationDataLive.value ?: return
        navigationData.pageNumbeText = text

        val totalPages: Int = notebook.atomicNotes.size
        navigationData.showNextButton = (index < totalPages - 1)
        navigationData.showPrevButton = (index > 0)
        navigationDataLive.setValue(navigationData)
    }

    override fun onCleared() {
        EventBus.getDefault().unregister(this)
        super.onCleared()
    }
}