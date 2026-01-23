package com.originb.inkwisenote2.modules.repositories;


import android.content.Context;
import androidx.room.Room;
import com.originb.inkwisenote2.common.NotesDatabase;
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository;
import com.originb.inkwisenote2.modules.queries.data.QueryRepository;
import org.koin.java.KoinJavaComponent;
import lombok.Getter;
import lombok.Setter;

//@Getter
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
        // Get dependencies from Koin since they are now managed by dependency injection
        notesDb = KoinJavaComponent.get(NotesDatabase.class);
        atomicNotesDomain = KoinJavaComponent.get(AtomicNotesDomain.class);
        handwrittenNoteRepository = KoinJavaComponent.get(HandwrittenNoteRepository.class);
        noteRelationRepository = KoinJavaComponent.get(NoteRelationRepository.class);
        queryRepository = KoinJavaComponent.get(QueryRepository.class);
        smartNotebookRepository = KoinJavaComponent.get(SmartNotebookRepository.class);
    }

    public SmartNotebookRepository getSmartNotebookRepository() {
        return smartNotebookRepository;
    }

    public HandwrittenNoteRepository getHandwrittenNoteRepository() {
        return handwrittenNoteRepository;
    }
}
