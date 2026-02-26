package org.basnalcorp.shared.domain

data class TextNote(
    var noteId: Long = 0,
    var bookId: Long = 0,
    var noteText: String? = null,
    var createdTimeMillis: Long = 0,
    var lastModifiedTimeMillis: Long = 0
)
