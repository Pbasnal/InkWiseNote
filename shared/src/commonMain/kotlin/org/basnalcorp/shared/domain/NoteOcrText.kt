package org.basnalcorp.shared.domain

data class NoteOcrText(
    var noteId: Long = 0,
    var extractedText: String = "",
    var noteHash: String = "",
    var createdTimeMillis: Long = 0,
    var lastModifiedTimeMillis: Long = 0
)
