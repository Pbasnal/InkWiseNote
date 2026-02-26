package org.basnalcorp.shared.domain

data class NoteTermFrequency(
    var id: Long = 0,
    var noteId: Long = 0,
    var term: String = "",
    var termFrequency: Long = 0
)
