package com.originb.inkwisenote2.modules.repositories

import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookEntity
import com.originb.inkwisenote2.modules.smartnotes.data.SmartBookPage
import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
class SmartNotebook {
    var smartBook: SmartBookEntity?
    var smartBookPages: MutableList<SmartBookPage?>?
    var atomicNotes: MutableList<AtomicNoteEntity?>?

    constructor(
        smartBook: SmartBookEntity?,
        smartBookPage: SmartBookPage?,
        atomicNoteEntity: AtomicNoteEntity?
    ) {
        this.smartBookPages = ArrayList()
        this.atomicNotes = ArrayList()

        this.smartBook = smartBook
        smartBookPages.add(smartBookPage)
        atomicNotes.add(atomicNoteEntity)
    }

    constructor(
        smartBook: SmartBookEntity?,
        smartBookPages: MutableList<SmartBookPage?>?,
        atomicNoteEntities: MutableList<AtomicNoteEntity?>?
    ) {
        this.smartBook = smartBook
        this.smartBookPages = smartBookPages
        this.atomicNotes = atomicNoteEntities
    }

    fun insertAtomicNoteAndPage(position: Int, atomicNote: AtomicNoteEntity?, newPage: SmartBookPage?) {
        atomicNotes!!.add(position, atomicNote)
        smartBookPages!!.add(position, newPage)
        var pageOrder = 0
        for (smartBookPage in smartBookPages!!) {
            smartBookPage.setPageOrder(pageOrder)
            pageOrder++
        }
    }

    fun removeNote(noteId: Long) {
        smartBookPages!!.removeIf { p: SmartBookPage? -> p.getNoteId() == noteId }
        atomicNotes!!.removeIf { p: AtomicNoteEntity? -> p.getNoteId() == noteId }
    }


    // Custom equals that compares only 'id'
    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true // Reference equality

        if (obj == null || javaClass != obj.javaClass) return false // Type check

        val that = obj as SmartNotebook

        if (that.smartBook == null) return false

        return smartBook.getBookId() == that.smartBook.getBookId()
    }

    // Custom hashCode that considers only 'id'
    override fun hashCode(): Int {
        return Objects.hash(smartBook.getBookId()) // Hash based on 'id'
    }
}
