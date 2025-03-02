package com.originb.inkwisenote.modules.repositories;


import android.content.Context;
import androidx.room.Room;
import com.originb.inkwisenote.data.notedata.PageSettings;
import com.originb.inkwisenote.io.sql.NotesDatabase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Repositories {
    private static Repositories instance;

    private SmartNotebookRepository smartNotebookRepository;
    private HandwrittenNoteRepository handwrittenNoteRepository;
    private NoteRelationRepository noteRelationRepository;

    private NotesDatabase notesDb;

    private PageSettings pageSettings;

    private Repositories() {
    }

    public static Repositories getInstance() {
        if (instance == null) {
            instance = new Repositories();
        }
        return instance;
    }

    public static void registerRepositories(Context appContext) {
        getInstance().registerRepositoriesInternal(appContext);
    }

    private void registerRepositoriesInternal(Context appContext) {
        notesDb = Room.databaseBuilder(appContext,
                        NotesDatabase.class, "NoteText.db")
                .fallbackToDestructiveMigration()
                .build();

        pageSettings = new PageSettings();

        smartNotebookRepository = new SmartNotebookRepository();
        handwrittenNoteRepository = new HandwrittenNoteRepository();
        noteRelationRepository = new NoteRelationRepository();
    }
}
