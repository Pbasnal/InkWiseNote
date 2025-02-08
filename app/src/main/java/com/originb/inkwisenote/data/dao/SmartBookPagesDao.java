package com.originb.inkwisenote.data.dao;

import androidx.room.*;
import com.originb.inkwisenote.data.entities.notedata.SmartBookEntity;
import com.originb.inkwisenote.data.entities.notedata.SmartBookPage;

import java.util.List;
import java.util.Set;

@Dao
public interface SmartBookPagesDao {
    @Query("SELECT * FROM smart_book_pages WHERE note_id = :noteId")
    List<SmartBookPage> getSmartBookPagesOfNote(long noteId);

    @Query("SELECT * FROM smart_book_pages WHERE book_id = :bookId")
    List<SmartBookPage> getSmartBookPages(long bookId);

    @Query("SELECT * FROM smart_book_pages")
    List<SmartBookPage> getAllSmartBookPages();

    @Query("SELECT * FROM smart_book_pages WHERE book_id IN (:bookIds)")
    List<SmartBookPage> getSmartBooksPages(Set<Long> bookIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSmartBook(SmartBookPage smartBookEntity);

    @Update
    int updateSmartBook(SmartBookPage smartBookEntity);

    @Query("DELETE FROM smart_book_pages WHERE book_id = :bookId")
    void deleteSmartBook(long bookId);
}
