package com.originb.inkwisenote.data.dao.noteocr;

import androidx.room.*;
import com.originb.inkwisenote.data.entities.noteocrdata.NoteOcrText;

import java.util.List;

@Dao
public interface NoteOcrTextDao {
    @Query("SELECT * FROM note_text WHERE note_id = :noteId")
    NoteOcrText readTextFromDb(Long noteId);

    @Query("SELECT * FROM note_text")
    List<NoteOcrText> getAllNoteText();

    @Query("SELECT * FROM note_text WHERE extracted_text LIKE '%' || :searchTerm || '%'")
    List<NoteOcrText> searchTextFromDb(String searchTerm);

    @Update
    int updateTextToDb(NoteOcrText noteOcrText);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTextToDb(NoteOcrText noteRelation);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTextToDb(List<NoteOcrText> noteRelation);

    @Query("DELETE FROM note_text WHERE note_id = :noteId")
    void deleteNoteText(Long noteId);
}
