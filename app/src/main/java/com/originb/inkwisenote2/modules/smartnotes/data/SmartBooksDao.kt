package com.originb.inkwisenote2.modules.smartnotes.data

import androidx.room.*

@Dao
interface SmartBooksDao {
    @Query("SELECT * FROM smart_books WHERE book_id = :bookId")
    fun getSmartbooksWithMatchingTitle(bookId: Long): SmartBookEntity?

    @Query("SELECT * FROM smart_books WHERE title like :title")
    fun getSmartbooksWithMatchingTitle(title: String?): List<SmartBookEntity?>?

    @get:Query("SELECT * FROM smart_books ")
    val allSmartBooks: List<SmartBookEntity?>?

    @Query("SELECT * FROM smart_books WHERE book_id IN (:bookIds)")
    fun getSmartBooks(bookIds: Set<Long?>?): List<SmartBookEntity?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSmartBook(smartBookEntity: SmartBookEntity?): Long

    @Update
    fun updateSmartBook(smartBookEntity: SmartBookEntity?): Int

    @Query("DELETE FROM smart_books WHERE book_id = :bookId")
    fun deleteSmartBook(bookId: Long)
}