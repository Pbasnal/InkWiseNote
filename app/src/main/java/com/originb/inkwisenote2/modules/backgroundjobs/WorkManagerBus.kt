package com.originb.inkwisenote2.modules.backgroundjobs

import android.content.Context
import androidx.room.RoomDatabase.Builder.build
import androidx.work.*
import androidx.work.WorkRequest.Builder.build
import androidx.work.WorkRequest.Builder.setInputData
import com.originb.inkwisenote2.modules.noterelation.worker.NoteRelationWorker
import com.originb.inkwisenote2.modules.noterelation.worker.TextProcessingWorker
import com.originb.inkwisenote2.modules.ocr.worker.TextParsingWorker

object WorkManagerBus {
    fun scheduleWorkForTextParsingForBook(context: Context?, bookId: Long?) {
        val inputData = Data.Builder()
            .putLong("book_id", bookId!!) // Path of the image file
            .build()

        val textParsingWork: OneTimeWorkRequest = Builder(TextParsingWorker::class.java)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context!!).enqueue(textParsingWork)
    }

    fun scheduleWorkForTextProcessingForBook(context: Context?, bookId: Long?) {
        val inputData = Data.Builder()
            .putLong("book_id", bookId!!) // Path of the image file
            .build()

        val textProcessingWork: OneTimeWorkRequest = Builder(TextProcessingWorker::class.java)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context!!).enqueue(textProcessingWork)
    }

    fun scheduleWorkForFindingRelatedNotesForBook(context: Context?, bookId: Long?) {
        val inputData = Data.Builder()
            .putLong("book_id", bookId!!) // Path of the image file
            .build()

        val noteRelationWorker: OneTimeWorkRequest = Builder(NoteRelationWorker::class.java)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context!!).enqueue(noteRelationWorker)
    }
}
