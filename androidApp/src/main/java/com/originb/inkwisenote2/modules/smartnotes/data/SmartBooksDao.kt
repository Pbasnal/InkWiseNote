package com.originb.inkwisenote2.modules.smartnotes.data

import androidx.room.*

@Dao
interface SmartBooksDao {
    @Query("SELECT * FROM smart_books WHERE book_id = :bookId")
    fun getSmartbook(bookId: Long): SmartBookEntity?

    @Query("SELECT * FROM smart_books WHERE title like :title")
    fun getSmartbooksWithMatchingTitle(title: String): MutableList<SmartBookEntity>

    @get:Query("SELECT * FROM smart_books ")
    val allSmartBooks: MutableList<SmartBookEntity>

    @Query("SELECT * FROM smart_books WHERE book_id IN (:bookIds)")
    fun getSmartBooks(bookIds: MutableSet<Long>): MutableList<SmartBookEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertSmartBook(smartBookEntity: SmartBookEntity): Long

    @Update
    fun updateSmartBook(smartBookEntity: SmartBookEntity): Int

    @Query("DELETE FROM smart_books WHERE book_id = :bookId")
    fun deleteSmartBook(bookId: Long)
}