package com.originb.inkwisenote.modules.repositories;


import android.content.Context;
import androidx.room.Room;
import com.originb.inkwisenote.common.NotesDatabase;
import com.originb.inkwisenote.modules.handwrittennotes.data.HandwrittenNoteRepository;
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

    private Repositories() {
    }

    public static Repositories getInstance() {
        if (instance == null) {
            instance = new Repositories();
        }
        return instance;
    }

    public static Repositories registerRepositories(Context appContext) {
        Repositories instance = getInstance();
        instance.registerRepositoriesInternal(appContext);

        return instance;
    }

    private void registerRepositoriesInternal(Context appContext) {
        notesDb = Room.databaseBuilder(appContext,
                        NotesDatabase.class, "NoteText.db")
                .fallbackToDestructiveMigration()
                .build();

        smartNotebookRepository = new SmartNotebookRepository();
        handwrittenNoteRepository = new HandwrittenNoteRepository();
        noteRelationRepository = new NoteRelationRepository();
    }
}
