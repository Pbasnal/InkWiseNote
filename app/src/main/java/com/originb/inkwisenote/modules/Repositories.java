package com.originb.inkwisenote.modules;


import android.content.ContextWrapper;
import com.originb.inkwisenote.io.BitmapRepository;
import com.originb.inkwisenote.io.NoteRepository;
import com.originb.inkwisenote.io.PageTemplateRepository;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Repositories {
    private static Repositories instance;

    private NoteRepository notesRepository;
    private BitmapRepository bitmapRepository;
    private PageTemplateRepository pageTemplateRepository;

    private Repositories() {
    }

    public static Repositories getInstance() {
        if (instance == null) {
            instance = new Repositories();
        }
        return instance;
    }

    public static void registerRepositories(ContextWrapper appContext) {
        getInstance().notesRepository = new NoteRepository(appContext.getFilesDir());
        getInstance().bitmapRepository = new BitmapRepository(appContext.getFilesDir());
        getInstance().pageTemplateRepository = new PageTemplateRepository(appContext.getFilesDir());
    }

    public static void initRepositories() {
        Repositories instance = getInstance();
        instance.getNotesRepository().loadAll();
        instance.getBitmapRepository().loadAllAsThumbnails();
        instance.getPageTemplateRepository().loadAll();
    }
}
