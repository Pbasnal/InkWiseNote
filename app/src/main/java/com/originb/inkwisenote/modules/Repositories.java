package com.originb.inkwisenote.modules;


import android.content.ContextWrapper;
import com.originb.inkwisenote.data.repositories.NoteRepository;
import com.originb.inkwisenote.io.NoteBitmapFiles;
import com.originb.inkwisenote.io.NoteMetaFiles;
import com.originb.inkwisenote.io.PageTemplateFiles;
//import com.originb.inkwisenote.io.ocr.TesseractsOcr;
import com.originb.inkwisenote.io.sql.NoteTextContract;
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

//    private TesseractsOcr tesseractsOcr;

    private NoteTextContract.NoteTextDbHelper noteTextDbHelper;

    private Repositories() {
    }

    public static Repositories getInstance() {
        if (instance == null) {
            instance = new Repositories();
        }
        return instance;
    }

    public static void registerRepositories(ContextWrapper appContext) {
        getInstance().noteMetaRepository = new NoteMetaFiles(appContext.getFilesDir());
        getInstance().bitmapRepository = new NoteBitmapFiles(appContext.getFilesDir());
        getInstance().pageTemplateFiles = new PageTemplateFiles(appContext.getFilesDir());
//        getInstance().tesseractsOcr = new TesseractsOcr(appContext);

        getInstance().noteTextDbHelper = new NoteTextContract.NoteTextDbHelper(appContext);
        getInstance().noteRepository = new NoteRepository();
    }

    public static void initRepositories() {
        Repositories instance = getInstance();
        instance.getNoteMetaRepository().loadAll();
        instance.getBitmapRepository().loadAllAsThumbnails();
        instance.getPageTemplateFiles().loadAll();
    }
}
