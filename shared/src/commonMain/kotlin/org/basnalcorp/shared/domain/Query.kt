package org.basnalcorp.shared.domain

data class Query(
    var name: String = "",
    var wordsToFind: String? = null,
    var wordsToIgnore: String? = null,
    var createdTimeMillis: Long = 0
)
