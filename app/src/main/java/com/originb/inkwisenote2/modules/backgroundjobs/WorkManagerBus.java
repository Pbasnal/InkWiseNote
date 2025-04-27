package com.originb.inkwisenote2.modules.backgroundjobs;

import android.content.Context;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.originb.inkwisenote2.modules.noterelation.worker.NoteRelationWorker;
import com.originb.inkwisenote2.modules.noterelation.worker.TextProcessingWorker;
import com.originb.inkwisenote2.modules.ocr.worker.TextParsingWorker;

public class WorkManagerBus {
    public static void scheduleWorkForTextParsingForBook(Context context, long bookId, long noteId) {
        Data inputData = new Data.Builder()
                .putLong("book_id", bookId) // Path of the image file
                .putLong("note_id", noteId)
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

    public static void scheduleWorkForFindingRelatedNotesForBook(Context context, long bookId, long noteId) {
        Data inputData = new Data.Builder()
                .putLong("book_id", bookId) // Path of the image file
                .putLong("note_id", noteId)
                .build();

        OneTimeWorkRequest noteRelationWorker = new OneTimeWorkRequest.Builder(NoteRelationWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueue(noteRelationWorker);
    }
}
