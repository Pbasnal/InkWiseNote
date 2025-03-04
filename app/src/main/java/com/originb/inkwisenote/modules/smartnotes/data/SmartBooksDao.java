package com.originb.inkwisenote.modules.smartnotes.data;

import androidx.room.*;

import java.util.List;
import java.util.Set;

@Dao
public interface SmartBooksDao {
    @Query("SELECT * FROM smart_books WHERE book_id = :bookId")
    SmartBookEntity getSmartbooksWithMatchingTitle(long bookId);

    @Query("SELECT * FROM smart_books WHERE title like :title")
    List<SmartBookEntity> getSmartbooksWithMatchingTitle(String title);

    @Query("SELECT * FROM smart_books ")
    List<SmartBookEntity> getAllSmartBooks();

    @Query("SELECT * FROM smart_books WHERE book_id IN (:bookIds)")
    List<SmartBookEntity> getSmartBooks(Set<Long> bookIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSmartBook(SmartBookEntity smartBookEntity);

    @Update
    int updateSmartBook(SmartBookEntity smartBookEntity);

    @Query("DELETE FROM smart_books WHERE book_id = :bookId")
    void deleteSmartBook(long bookId);
}