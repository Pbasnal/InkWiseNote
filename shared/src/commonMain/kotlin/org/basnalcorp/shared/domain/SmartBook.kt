package org.basnalcorp.shared.domain

data class SmartBook(
    var bookId: Long = 0,
    var title: String? = null,
    var createdTimeMillis: Long = 0,
    var lastModifiedTimeMillis: Long = 0
)
