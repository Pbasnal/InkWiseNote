package org.basnalcorp.shared.data.repository

import org.basnalcorp.shared.domain.SmartBook

interface SmartBooksRepository {
    fun insert(smartBook: SmartBook): Long
    fun update(smartBook: SmartBook)
    fun delete(bookId: Long)
    fun get(bookId: Long): SmartBook?
    fun getByIds(bookIds: Set<Long>): List<SmartBook>
    fun getAll(): List<SmartBook>
    fun getByTitlePattern(pattern: String): List<SmartBook>
}
