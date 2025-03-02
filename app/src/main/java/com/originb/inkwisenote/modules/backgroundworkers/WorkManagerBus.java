package com.originb.inkwisenote.modules.backgroundworkers;

import android.content.Context;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class WorkManagerBus {
    public static void scheduleWorkForTextParsingForBook(Context context, Long bookId) {
        Data inputData = new Data.Builder()
                .putLong("book_id", bookId) // Path of the image file
                .build();

        OneTimeWorkRequest textParsingWork = new OneTimeWorkRequest.Builder(TextParsingWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueue(textParsingWork);
    }

    public static void scheduleWorkForTextProcessingForBook(Context context, Long bookId) {
        Data inputData = new Data.Builder()
                .putLong("book_id", bookId) // Path of the image file
                .build();

        OneTimeWorkRequest textProcessingWork = new OneTimeWorkRequest.Builder(TextProcessingWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueue(textProcessingWork);
    }

    public static void scheduleWorkForFindingRelatedNotesForBook(Context context, Long bookId) {
        Data inputData = new Data.Builder()
                .putLong("book_id", bookId) // Path of the image file
                .build();

        OneTimeWorkRequest noteRelationWorker = new OneTimeWorkRequest.Builder(NoteRelationWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueue(noteRelationWorker);
    }
}
