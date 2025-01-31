package com.originb.inkwisenote.modules.noteoperations;

import android.content.Context;
import android.graphics.Bitmap;
import com.originb.inkwisenote.data.config.AppState;
import com.originb.inkwisenote.data.dao.NoteTaskStatusDao;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskName;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStage;
import com.originb.inkwisenote.data.entities.tasks.NoteTaskStatus;
import com.originb.inkwisenote.data.entities.tasks.TfIdfRelationTasks;
import com.originb.inkwisenote.data.notedata.NoteEntity;
import com.originb.inkwisenote.data.notedata.PageTemplate;
import com.originb.inkwisenote.modules.backgroundworkers.WorkManagerBus;
import com.originb.inkwisenote.modules.functionalUtils.Try;
import com.originb.inkwisenote.modules.messaging.BackgroundOps;
import com.originb.inkwisenote.modules.repositories.NoteRepository;
import com.originb.inkwisenote.modules.repositories.Repositories;

import java.util.Objects;

public class NoteOperations {

    private final NoteRepository noteRepository;
    private final NoteTaskStatusDao noteTaskStatusDao;

    private final Context appContext;

    public NoteOperations(Context context) {
        noteRepository = Repositories.getInstance().getNoteRepository();
        noteTaskStatusDao = Repositories.getInstance().getNotesDb().noteTaskStatusDao();
        appContext = context;
    }

    public void updateNote(NoteEntity noteEntity, Bitmap noteBitmap, PageTemplate pageTemplate) {
        BackgroundOps.execute(() -> {
            noteRepository.updateNote(noteEntity.getNoteMeta(),
                    noteBitmap,
                    pageTemplate);

            // does the same thing as above call. So no need
            // updateNoteMeta(noteEntity.getNoteMeta());
            NoteTaskStatus jobStatus = noteTaskStatusDao.getNoteStatus(noteEntity.getNoteId(), NoteTaskName.TF_IDF_RELATION);

            if (Objects.isNull(jobStatus)) {
                noteTaskStatusDao.insertNoteTask(TfIdfRelationTasks.newTask(noteEntity.getNoteId()));
            } else {
                noteTaskStatusDao.updateNoteTask(TfIdfRelationTasks.newTask(noteEntity.getNoteId()));
            }

            AppState.getInstance().setNoteStatus(noteEntity.getNoteId(), NoteTaskStage.TEXT_PARSING);
            WorkManagerBus.scheduleWorkForTextParsing(appContext, noteEntity.getNoteId());
        });
    }

    public void deleteNote(Long noteId) {
        BackgroundOps.execute(() -> noteRepository.deleteNote(noteId));

    }
}
