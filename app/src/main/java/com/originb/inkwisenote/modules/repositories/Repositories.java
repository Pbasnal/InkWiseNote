package com.originb.inkwisenote.modules.repositories;


import android.content.Context;
import androidx.room.Room;
import com.originb.inkwisenote.data.notedata.PageSettings;
import com.originb.inkwisenote.io.NoteBitmapFiles;
import com.originb.inkwisenote.io.NoteMetaFiles;
import com.originb.inkwisenote.io.PageTemplateFiles;
//import com.originb.inkwisenote.io.ocr.TesseractsOcr;
import com.originb.inkwisenote.io.sql.NotesDatabase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Repositories {
    private static Repositories instance;

    private NoteMetaFiles noteMetaRepository;
    private NoteBitmapFiles bitmapRepository;
    private PageTemplateFiles pageTemplateFiles;
    private NoteRepository noteRepository;
    private SmartNotebookRepository smartNotebookRepository;
    // private TesseractsOcr TesseractsOcr;

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

        noteMetaRepository = new NoteMetaFiles(appContext.getFilesDir());
        bitmapRepository = new NoteBitmapFiles(appContext.getFilesDir());
        pageTemplateFiles = new PageTemplateFiles(appContext.getFilesDir());
//      tesseractsOcr = new TesseractsOcr(appContext);
        pageSettings = new PageSettings();

        noteRepository = new NoteRepository();
        smartNotebookRepository = new SmartNotebookRepository();
    }

    public static void initRepositories() {
        Repositories instance = getInstance();
        instance.getNoteMetaRepository().loadAll();
        instance.getBitmapRepository().loadAllAsThumbnails();
        instance.getPageTemplateFiles().loadAll();
    }
}
