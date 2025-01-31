package com.originb.inkwisenote.modules.backgroundworkers;

import android.content.Context;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.originb.inkwisenote.modules.uieventworkers.NoteDeletionWorker;

public class WorkManagerBus {

    public static void scheduleWorkForTextParsing(Context context, Long noteId) {
        Data inputData = new Data.Builder()
                .putLong("note_id", noteId) // Path of the image file
                .build();

        OneTimeWorkRequest textParsingWork = new OneTimeWorkRequest.Builder(TextParsingWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueue(textParsingWork);
    }

    public static void scheduleWorkForTextProcessing(Context context, Long noteId) {
        Data inputData = new Data.Builder()
                .putLong("note_id", noteId) // Path of the image file
                .build();

        OneTimeWorkRequest textProcessingWork = new OneTimeWorkRequest.Builder(TextProcessingWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueue(textProcessingWork);
    }

    public static void scheduleWorkForFindingRelatedNotes(Context context, Long noteId) {
        Data inputData = new Data.Builder()
                .putLong("note_id", noteId) // Path of the image file
                .build();

        OneTimeWorkRequest noteRelationWorker = new OneTimeWorkRequest.Builder(NoteRelationWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueue(noteRelationWorker);
    }
}
