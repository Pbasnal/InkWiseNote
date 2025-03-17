package com.originb.inkwisenote2.modules.smartnotes.data

import androidx.room.*

@Dao
interface SmartBookPagesDao {
    @Query("SELECT * FROM smart_book_pages WHERE note_id = :noteId")
    fun getSmartBookPagesOfNote(noteId: Long): List<SmartBookPage?>?

    @Query("SELECT * FROM smart_book_pages WHERE note_id In (:noteIds)")
    fun getSmartBookPagesOfNote(noteIds: Set<Long>?): MutableList<SmartBookPage?>

    @Query("SELECT * FROM smart_book_pages WHERE book_id = :bookId")
    fun getSmartBookPages(bookId: Long): MutableList<SmartBookPage?>?

    @get:Query("SELECT * FROM smart_book_pages")
    val allSmartBookPages: List<SmartBookPage?>?

    @Query("SELECT * FROM smart_book_pages WHERE book_id IN (:bookIds)")
    fun getSmartBooksPages(bookIds: Set<Long>?): List<SmartBookPage?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSmartBookPage(smartBookEntity: SmartBookPage?): Long

    @Update
    fun updateSmartBookPage(smartBookEntity: SmartBookPage?): Int

    @Update
    fun updateSmartBookPage(smartBookEntity: List<SmartBookPage?>?): Int

    @Query("DELETE FROM smart_book_pages WHERE book_id = :bookId")
    fun deleteSmartBookPages(bookId: Long)

    @Query("DELETE FROM smart_book_pages WHERE note_id = :noteId")
    fun deleteNotePages(noteId: Long)
}
