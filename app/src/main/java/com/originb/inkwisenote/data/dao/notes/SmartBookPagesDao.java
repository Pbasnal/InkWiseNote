package com.originb.inkwisenote.data.dao.notes;

import androidx.room.*;
import com.originb.inkwisenote.data.entities.notedata.SmartBookPage;

import java.util.List;
import java.util.Set;

@Dao
public interface SmartBookPagesDao {
    @Query("SELECT * FROM smart_book_pages WHERE note_id = :noteId")
    List<SmartBookPage> getSmartBookPagesOfNote(long noteId);

    @Query("SELECT * FROM smart_book_pages WHERE note_id In (:noteIds)")
    List<SmartBookPage> getSmartBookPagesOfNote(Set<Long> noteIds);

    @Query("SELECT * FROM smart_book_pages WHERE book_id = :bookId")
    List<SmartBookPage> getSmartBookPages(long bookId);

    @Query("SELECT * FROM smart_book_pages")
    List<SmartBookPage> getAllSmartBookPages();

    @Query("SELECT * FROM smart_book_pages WHERE book_id IN (:bookIds)")
    List<SmartBookPage> getSmartBooksPages(Set<Long> bookIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSmartBookPage(SmartBookPage smartBookEntity);

    @Update
    int updateSmartBookPage(SmartBookPage smartBookEntity);

    @Update
    int updateSmartBookPage(List<SmartBookPage> smartBookEntity);

    @Query("DELETE FROM smart_book_pages WHERE book_id = :bookId")
    void deleteSmartBookPages(long bookId);
}
