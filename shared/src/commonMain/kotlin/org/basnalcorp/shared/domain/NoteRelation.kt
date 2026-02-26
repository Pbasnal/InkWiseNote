package org.basnalcorp.shared.domain

data class NoteRelation(
    var id: Int = 0,
    var noteId: Long = 0,
    var relatedNoteId: Long = 0,
    var bookId: Long = 0,
    var relatedBookId: Long = 0,
    var relationType: Int = 0
)
