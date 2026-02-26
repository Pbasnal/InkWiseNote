package org.basnalcorp.shared.domain

data class AtomicNote(
    var noteId: Long = 0,
    var filename: String? = null,
    var filepath: String? = null,
    var noteType: String? = null,
    var pageTemplateId: Long = 0,
    var createdTimeMillis: Long = 0,
    var lastModifiedTimeMillis: Long = 0
)
