package com.originb.inkwisenote2.modules.repositories;


import android.content.Context;
import androidx.room.Room;
import com.originb.inkwisenote2.common.NotesDatabase;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.queries.data.QueryRepository;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Repositories {
    private static Repositories instance;

    private QueryRepository queryRepository;
    private SmartNotebookRepository smartNotebookRepository;
    private HandwrittenNoteRepository handwrittenNoteRepository;
    private NoteRelationRepository noteRelationRepository;
    private AtomicNotesDomain atomicNotesDomain;

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

        handwrittenNoteRepository = new HandwrittenNoteRepository();
        noteRelationRepository = new NoteRelationRepository();
        atomicNotesDomain = new AtomicNotesDomain();
        queryRepository = new QueryRepository();
        smartNotebookRepository = new SmartNotebookRepository();
    }
}
