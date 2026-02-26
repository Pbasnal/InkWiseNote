package org.basnalcorp.shared.domain

/**
 * Aggregate: a smart book with its pages and atomic notes.
 * Used by SmartNotebookRepository for list/detail operations.
 */
data class SmartNotebook(
    var smartBook: SmartBook,
    var smartBookPages: MutableList<SmartBookPage>,
    var atomicNotes: MutableList<AtomicNote>
)
