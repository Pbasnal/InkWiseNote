package com.originb.inkwisenote2.modules.repositories

import android.content.Context
import androidx.room.Room.databaseBuilder
import com.originb.inkwisenote2.common.NotesDatabase
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import lombok.Getter
import lombok.Setter


@Getter
@Setter
class Repositories private constructor() {
    private var smartNotebookRepository: SmartNotebookRepository? = null
    private var handwrittenNoteRepository: HandwrittenNoteRepository? = null
    private var noteRelationRepository: NoteRelationRepository? = null

    private var notesDb: NotesDatabase? = null

    private fun registerRepositoriesInternal(appContext: Context) {
        notesDb = databaseBuilder(
            appContext,
            NotesDatabase::class.java, "NoteText.db"
        )
            .fallbackToDestructiveMigration()
            .build()

        smartNotebookRepository = SmartNotebookRepository()
        handwrittenNoteRepository = HandwrittenNoteRepository()
        noteRelationRepository = NoteRelationRepository()
    }

    companion object {
        var instance: Repositories? = null
            get() {
                if (field == null) {
                    field = Repositories()
                }
                return field
            }
            private set

        fun registerRepositories(appContext: Context): Repositories? {
            val instance = instance
            instance!!.registerRepositoriesInternal(appContext)

            return instance
        }
    }
}
