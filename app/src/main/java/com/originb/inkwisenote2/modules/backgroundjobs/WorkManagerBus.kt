package com.originb.inkwisenote2.modules.backgroundjobs

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.originb.inkwisenote2.modules.noterelation.worker.NoteRelationWorker
import com.originb.inkwisenote2.modules.noterelation.worker.TextProcessingWorker
import com.originb.inkwisenote2.modules.ocr.worker.TextParsingWorker

object WorkManagerBus {
    @JvmStatic
    fun scheduleWorkForTextParsingForBook(context: Context, bookId: Long, noteId: Long) {
        val inputData = Data.Builder()
            .putLong("book_id", bookId) // Path of the image file
            .putLong("note_id", noteId)
            .build()

        val textParsingWork = OneTimeWorkRequest.Builder(TextParsingWorker::class.java)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(textParsingWork)
    }

    @JvmStatic
    fun scheduleWorkForTextProcessingForBook(context: Context, bookId: Long, noteId: Long) {
        val inputData = Data.Builder()
            .putLong("book_id", bookId) // Path of the image file
            .putLong("note_id", noteId)
            .build()

        val textProcessingWork = OneTimeWorkRequest.Builder(TextProcessingWorker::class.java)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(textProcessingWork)
    }

    @JvmStatic
    fun scheduleWorkForFindingRelatedNotesForBook(context: Context, bookId: Long, noteId: Long) {
        val inputData = Data.Builder()
            .putLong("book_id", bookId) // Path of the image file
            .putLong("note_id", noteId)
            .build()

        val noteRelationWorker = OneTimeWorkRequest.Builder(NoteRelationWorker::class.java)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(noteRelationWorker)
    }
}
