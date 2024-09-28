package com.originb.inkwisenote.modules;


import android.content.ContextWrapper;
import com.originb.inkwisenote.io.NoteBitmapFiles;
import com.originb.inkwisenote.io.NoteMetaFiles;
import com.originb.inkwisenote.io.PageTemplateFiles;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Repositories {
    private static Repositories instance;

    private NoteMetaFiles notesRepository;
    private NoteBitmapFiles bitmapRepository;
    private PageTemplateFiles pageTemplateFiles;

    private Repositories() {
    }

    public static Repositories getInstance() {
        if (instance == null) {
            instance = new Repositories();
        }
        return instance;
    }

    public static void registerRepositories(ContextWrapper appContext) {
        getInstance().notesRepository = new NoteMetaFiles(appContext.getFilesDir());
        getInstance().bitmapRepository = new NoteBitmapFiles(appContext.getFilesDir());
        getInstance().pageTemplateFiles = new PageTemplateFiles(appContext.getFilesDir());
    }

    public static void initRepositories() {
        Repositories instance = getInstance();
        instance.getNotesRepository().loadAll();
        instance.getBitmapRepository().loadAllAsThumbnails();
        instance.getPageTemplateFiles().loadAll();
    }
}
