package org.basnalcorp.shared.domain

data class HandwrittenNote(
    var noteId: Long = 0,
    var bookId: Long = 0,
    var bitmapFilePath: String? = null,
    var bitmapHash: String? = null,
    var pageTemplateFilePath: String? = null,
    var pageTemplateHash: String? = null,
    var createdTimeMillis: Long = 0,
    var lastModifiedTimeMillis: Long = 0
)
