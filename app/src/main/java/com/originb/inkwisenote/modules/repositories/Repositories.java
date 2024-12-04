package com.originb.inkwisenote.modules.repositories;


import android.content.Context;
import com.originb.inkwisenote.data.config.PageSettings;
import com.originb.inkwisenote.io.NoteBitmapFiles;
import com.originb.inkwisenote.io.NoteMetaFiles;
import com.originb.inkwisenote.io.PageTemplateFiles;
//import com.originb.inkwisenote.io.ocr.TesseractsOcr;
import com.originb.inkwisenote.io.sql.NoteTermFrequencyContract;
import com.originb.inkwisenote.io.sql.NoteTextContract;
import com.originb.inkwisenote.io.sql.TextProcessingJobContract;
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

    // private TesseractsOcr TesseractsOcr;

    private NoteTextContract.NoteTextDbHelper noteTextDbHelper;
    private TextProcessingJobContract.TextProcessingDbQueries textProcessingJobDbHelper;
    private NoteTermFrequencyContract.NoteTermFrequencyDbQueries noteTermFrequencyDbQueries;

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
        noteMetaRepository = new NoteMetaFiles(appContext.getFilesDir());
        bitmapRepository = new NoteBitmapFiles(appContext.getFilesDir());
        pageTemplateFiles = new PageTemplateFiles(appContext.getFilesDir());
//      tesseractsOcr = new TesseractsOcr(appContext);
        pageSettings = new PageSettings();

        noteRepository = new NoteRepository();
        noteTextDbHelper = new NoteTextContract.NoteTextDbHelper(appContext);
        textProcessingJobDbHelper = new TextProcessingJobContract.TextProcessingDbQueries(appContext);
        noteTermFrequencyDbQueries = new NoteTermFrequencyContract.NoteTermFrequencyDbQueries(appContext);
    }

    public static void initRepositories() {
        Repositories instance = getInstance();
        instance.getNoteMetaRepository().loadAll();
        instance.getBitmapRepository().loadAllAsThumbnails();
        instance.getPageTemplateFiles().loadAll();
    }
}
