package com.originb.inkwisenote2.modules.smartnotes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.common.Strings.isNotEmpty
import com.originb.inkwisenote2.common.Strings.isNullOrWhitespace
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps
import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
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
import java.lang.String
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import kotlin.Any
import kotlin.Array
import kotlin.Boolean
import kotlin.Comparator
import kotlin.Exception
import kotlin.Int
import kotlin.Long
import kotlin.plus
import kotlin.synchronized

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
        val smartNotebookUpdate = getSmartNotebookUpdate().getValue()
        if (smartNotebookUpdate == null) return
        val notebook = smartNotebookUpdate.smartNotebook
        if (notebook == null) return

        BackgroundOps.execute(
            Runnable { smartNotebookRepository.bookExists(notebook) },
            ifNotebookIsSaved
        )
    }

    fun setNotebookTitle(notebookTitle: String?) {
        val notebookUpdate = smartNotebookUpdate.getValue()
        notebookUpdate!!.smartNotebook!!.smartBook!!.setTitle(notebookTitle)
        notebookUpdate.notbookUpdateType = SmartNotebookUpdateType.NOTEBOOK_TITLE_UPDATED

        smartNotebookUpdate.setValue(notebookUpdate)
    }

    class SmartNotebookUpdate {
        var notbookUpdateType: SmartNotebookUpdateType = SmartNotebookUpdateType.NOTE_UPDATE
        var smartNotebook: SmartNotebook? = null
        var atomicNote: AtomicNoteEntity? = null
        var indexOfUpdatedNote: Int = -1

        companion object {
            fun fromNotebook(smartNotebook: SmartNotebook): SmartNotebookUpdate {
                val smartNotebookUpdate = SmartNotebookUpdate()
                smartNotebookUpdate.smartNotebook = smartNotebook
                return smartNotebookUpdate
            }

            fun noteDeleted(smartNotebook: SmartNotebook, atomicNote: AtomicNoteEntity?): SmartNotebookUpdate {
                val smartNotebookUpdate = SmartNotebookUpdate()
                smartNotebookUpdate.atomicNote = atomicNote
                smartNotebookUpdate.smartNotebook = smartNotebook
                smartNotebookUpdate.notbookUpdateType = SmartNotebookUpdateType.NOTE_DELETED
                return smartNotebookUpdate
            }

            fun fromNoteAndBook(
                updatedNotebook: SmartNotebook,
                indexOfUpdatedNote: Int
            ): SmartNotebookUpdate {
                val smartNotebookUpdate = SmartNotebookUpdate()
                smartNotebookUpdate.smartNotebook = updatedNotebook
                smartNotebookUpdate.indexOfUpdatedNote = indexOfUpdatedNote
                return smartNotebookUpdate
            }

            fun notebookDeleted(deletedNotebook: SmartNotebook): SmartNotebookUpdate {
                val smartNotebookUpdate = SmartNotebookUpdate()
                smartNotebookUpdate.smartNotebook = deletedNotebook
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
        if (!isNullOrWhitespace(bookTitle)) {
            this.workingNotePath = Paths.get(workingPath, bookTitle).toString()
        }

        BackgroundOps.executeOpt(
            { getSmartNotebook(bookId, bookTitle, noteIdsString) },
            { notebook ->
                smartNotebookUpdate.setValue(SmartNotebookUpdate.Companion.fromNotebook(notebook!!))
                notebookTitle.setValue(notebook.smartBook!!.getTitle())
                createdTimeMillis.setValue(notebook.smartBook!!.getCreatedTimeMillis())
                updatePageNumberText()
                if (bookTitle == null) {
                    var booktTitleFromDb = notebook.smartBook!!.getTitle()
                    booktTitleFromDb =
                        if (booktTitleFromDb != null) booktTitleFromDb else String.valueOf(notebook.smartBook!!.getCreatedTimeMillis())
                    this.workingNotePath = Paths.get(workingPath, booktTitleFromDb).toString()
                }
            }
        )
    }

    fun navigateToPageIndex(pageIndex: Int) {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook
        if (notebook == null) return
        if (pageIndex < notebook.getAtomicNotes().size()) {
            currentPageIndexLive.setValue(pageIndex)
            updatePageNumberText()
        }
    }

    fun navigateToNextPage() {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook
        if (notebook == null) return

        val currentIndex: Int = currentPageIndexLive.getValue()!!
        val nextIndex = currentIndex + 1
        if (nextIndex < notebook.getAtomicNotes().size()) {
            currentPageIndexLive.setValue(nextIndex)
            updatePageNumberText()
        }
    }

    fun navigateToPreviousPage() {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook
        if (notebook == null) return

        val currentIndex: Int = currentPageIndexLive.getValue()!!
        val prevIndex = currentIndex - 1

        if (prevIndex >= 0) {
            currentPageIndexLive.setValue(prevIndex)
            updatePageNumberText()
        }
    }

    fun addNewPage() {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook
        if (notebook == null) return

        val currentIndex: Int = currentPageIndexLive.getValue()!!
        val indexToInsertAt = currentIndex + 1

        execute(Runnable {
            val newAtomicNote = atomicNotesDomain.saveAtomicNote(
                constructAtomicNote(
                    "",
                    workingNotePath,
                    NoteType.NOT_SET
                )
            )
            val newSmartPage = smartNotebookRepository.newSmartBookPage(
                notebook.smartBook!!, newAtomicNote, indexToInsertAt
            )

            notebook.insertAtomicNoteAndPage(indexToInsertAt, newAtomicNote, newSmartPage)
            notebook
        }, Runnable { updatedNotebook ->
            smartNotebookUpdate.setValue(
                SmartNotebookUpdate.Companion.fromNoteAndBook(
                    updatedNotebook,
                    indexToInsertAt
                )
            )
            currentPageIndexLive.setValue(indexToInsertAt)
            updatePageNumberText()
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotebookDelete(notebookDeleted: NotebookDeleted) {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook
        val deletedNotebook = notebookDeleted.smartNotebook
        if (notebook == null || deletedNotebook!!.smartBook!!.getBookId() != notebook.smartBook!!.getBookId()) return
        deleteNotebookFolder(notebook.smartBook!!.getTitle())
        smartNotebookUpdate.setValue(SmartNotebookUpdate.Companion.notebookDeleted(deletedNotebook))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNoteDelete(noteDeleted: NoteDeleted) {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook
        if (notebook == null) return

        val noteId = noteDeleted.atomicNote!!.getNoteId()
        notebook.removeNote(noteId)

        if (notebook.atomicNotes.isEmpty()) {
            execute(
                Runnable { smartNotebookRepository.deleteSmartNotebook(notebook) },
                Runnable { deleteNotebookFolder(notebook.smartBook!!.getTitle()) })
            return
        }

        smartNotebookUpdate.setValue(SmartNotebookUpdate.Companion.noteDeleted(notebook, noteDeleted.atomicNote))

        val currentPageIndex: Int = currentPageIndexLive.getValue()!!
        if (currentPageIndex == notebook.getAtomicNotes().size()) {
            currentPageIndexLive.setValue(currentPageIndex - 1)
        }

        updatePageNumberText()
    }

    private fun deleteNotebookFolder(smartNotebookTitle: kotlin.String?) {
        var smartNotebookTitle = smartNotebookTitle
        if (smartNotebookTitle == null) {
            smartNotebookTitle = ""
        }
        val notebookPath = Paths.get(workingNotePath, smartNotebookTitle)
        try {
            Files.walk(notebookPath)
                .sorted(Comparator.reverseOrder<Path?>()) // Delete children before parent
                .forEach { path: Path? ->
                    try {
                        Files.delete(path)
                    } catch (e: IOException) {
                        // Handle exception (file might be locked, etc)
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
        newNotebookPath: kotlin.String,
        noteHolderData: NoteHolderData?
    ) {
        // Store the old path to move files
        val oldFilePath = atomicNote.getFilepath()

        // Update the filepath
        atomicNote.setFilepath(newNotebookPath)

        // Ensure directory exists
        val directory = File(newNotebookPath)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Move existing files if they exist
        moveFileIfExists(oldFilePath, newNotebookPath, atomicNote.getFilename() + ".png")
        moveFileIfExists(oldFilePath, newNotebookPath, atomicNote.getFilename() + "-t.png")
        moveFileIfExists(oldFilePath, newNotebookPath, atomicNote.getFilename() + ".pt")
        moveFileIfExists(oldFilePath, newNotebookPath, atomicNote.getFilename() + ".md")

        execute(Runnable {
            atomicNotesDomain.updateAtomicNote(atomicNote)
            saveCurrentNote(atomicNote, noteHolderData)
        })
    }

    /**
     * Moves a file from source to destination if it exists
     */
    private fun moveFileIfExists(sourcePath: kotlin.String?, destPath: kotlin.String?, filename: kotlin.String) {
        try {
            val sourceFile = File(sourcePath, filename)
            if (sourceFile.exists()) {
                val destFile = File(destPath, filename)
                if (!sourceFile.renameTo(destFile)) {
                    logger.error("Failed to move file: " + filename)
                }
            }
        } catch (e: Exception) {
            logger.exception("Error moving file: " + filename, e)
        }
    }

    fun updateTitle(updatedTitle: kotlin.String): Boolean {
        if (smartNotebookUpdate.getValue() == null) return false
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook
        if (notebook == null) return false

        if (isNotEmpty(updatedTitle)) {
            notebook.getSmartBook().setTitle(updatedTitle)
            notebookTitle.setValue(updatedTitle)
            return true
        }
        return false
    }

    fun renameNotebookFolderName(newNotebookPath: kotlin.String, oldNotebookTitle: kotlin.String?): Boolean {
        val newFolder = File(newNotebookPath)

        var updateNotesFolderName = true

        val oldFolder = File(workingNotePath + "/" + oldNotebookTitle)
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
        if (notebook == null || title == null) return

        notebook.getSmartBook().setTitle(title)
        if (notebook.smartBook!!.getBookId() > -1) {
            execute(Runnable { smartNotebookRepository.updateNotebook(notebook, getApplication<Application?>()) })
        } else {
            execute(Runnable { smartNotebookRepository.saveSmartNotebook(notebook, getApplication<Application?>()) })
        }
    }

    fun saveCurrentNote(atomicNote: AtomicNoteEntity?, noteData: NoteHolderData?) {
        if (atomicNote == null || noteData == null) {
            logger.error("Attempted to save null note or note data")
            return
        }

        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook
        if (notebook == null) return

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
                notebook.smartBook!!.getBookId(),
                atomicNote,
                noteData.bitmap,
                noteData.pageTemplate,
                noteData.strokes,
                getApplication<Application?>()
            )

            NoteType.TEXT_NOTE -> {
                // Save to database
                val textNoteEntity = textNotesDao.getTextNoteForNote(atomicNote.getNoteId())
                if (textNoteEntity == null) {
                    textNoteEntity = TextNoteEntity(
                        atomicNote.getNoteId(),
                        notebook.smartBook!!.getBookId()
                    )
                    textNotesDao.insertTextNote(textNoteEntity)
                }
                textNoteEntity.setNoteText(noteData.noteText)
                textNotesDao.updateTextNote(textNoteEntity)

                // Save to markdown file
                saveNoteToMarkdownFile(atomicNote, noteData.noteText)

                // Post event
                EventBus.getDefault().post(
                    TextNoteSaved(
                        notebook.smartBook!!.getBookId(),
                        atomicNote,
                        getApplication<Application?>()
                    )
                )
            }

            else -> {}
        }
    }

    /**
     * Save note text to a markdown file
     */
    private fun saveNoteToMarkdownFile(note: AtomicNoteEntity, noteText: kotlin.String?) {
        execute(Runnable {
            val filename = note.getFilename() + ".md"
            // Ensure we're using the notebook's directory path
            var notebookDir = workingNotePath
            if (isNullOrWhitespace(notebookDir)) {
                notebookDir = note.getFilepath()
            }
            val file = File(notebookDir, filename)
            try {
                FileWriter(file).use { writer ->
                    writer.write(noteText)
                    return@execute true
                }
            } catch (e: IOException) {
                logger.exception("Error saving markdown file", e)
                return@execute false
            }
        }, Runnable { success ->
            if (!success) {
                logger.error("Failed to save markdown file for note: " + note.getNoteId())
            }
        })
    }

    val currentNote: AtomicNoteEntity?
        get() {
            // todo: handle this null case
            if (smartNotebookUpdate.getValue() == null) {
                return null
            }

            val currentNoteIndex: Int = currentPageIndexLive.getValue()!!
            val notebook = smartNotebookUpdate.getValue()!!.smartNotebook!!
            return notebook.getAtomicNotes().get(currentNoteIndex)
        }

    private fun getSmartNotebook(
        bookIdToOpen: Long?,
        bookTitle: kotlin.String?,
        noteIdsString: kotlin.String?
    ): Optional<SmartNotebook?> {
        if (bookIdToOpen != null && bookIdToOpen != -1L) {
            return smartNotebookRepository.getSmartNotebooks(bookIdToOpen)
        } else if (noteIdsString != null && !noteIdsString.isEmpty()) {
            val noteIds: Array<kotlin.String?> =
                noteIdsString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val noteIdsSet = Arrays.stream<kotlin.String?>(noteIds)
                .map<Long?> { s: Function<in R?, out V?>? -> s.toLong() }
                .collect(Collectors.toSet())

            val smartNotebooks = smartNotebookRepository.getSmartNotebooksForNoteIds(noteIdsSet)
            if (smartNotebooks.size == 1) { // if all the notes belong to an existing notebook
                val notebookWithAllNotes = smartNotebooks.stream().findFirst()
                val noteIdsInNotebook = notebookWithAllNotes.get().atomicNotes
                    .stream().map<Long?> { obj: Function<in R?, out V?>? -> obj.getNoteId() }
                    .collect(Collectors.toSet())

                noteIdsInNotebook.removeAll(noteIdsSet)
                if (!noteIdsInNotebook.isEmpty()) return notebookWithAllNotes
            }

            return smartNotebookRepository.getVirtualSmartNotebooks(bookTitle, noteIdsSet)
        }

        return smartNotebookRepository.initializeNewSmartNotebook(
            "", workingNotePath, NoteType.NOT_SET
        )
    }

    private fun updatePageNumberText() {
        val notebook = smartNotebookUpdate.getValue()!!.smartNotebook
        val index = currentPageIndexLive.getValue()
        if (notebook == null || index == null) return

        val text = (index + 1).toString() + "/" + notebook.getAtomicNotes().size()

        val navigationData = navigationDataLive.getValue()
        navigationData!!.pageNumbeText = text

        val totalPages: Int = notebook.getAtomicNotes().size()
        navigationData.showNextButton = (index < totalPages - 1)
        navigationData.showPrevButton = (index > 0)
        navigationDataLive.setValue(navigationData)
    }

    override fun onCleared() {
        EventBus.getDefault().unregister(this)
        super.onCleared()
    }
}