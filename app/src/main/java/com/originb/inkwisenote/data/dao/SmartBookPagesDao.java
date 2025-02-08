package com.originb.inkwisenote.data.dao;

import androidx.room.*;
import com.originb.inkwisenote.data.entities.notedata.SmartBookEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookPage;

import java.util.List;
import java.util.Set;

@Dao
public interface SmartBookPagesDao {
    @Query("SELECT * FROM smart_book_pages WHERE book_id = :bookId")
    List<SmartBookPage> getSmartBookPages(Long bookId);

    @Query("SELECT * FROM smart_book_pages WHERE book_id IN (:bookIds)")
    List<SmartBookPage> getSmartBooksPages(Set<Long> bookIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSmartBook(SmartBookPage smartBookEntity);

    @Update
    int updateSmartBook(SmartBookPage smartBookEntity);

    @Query("DELETE FROM smart_book_pages WHERE book_id = :bookId")
    void deleteSmartBook(Long bookId);
}
