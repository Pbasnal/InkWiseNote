package com.originb.inkwisenote2

import androidx.room.Room
import com.originb.inkwisenote2.common.NotesDatabase
import com.originb.inkwisenote2.modules.admin.AdminViewModel
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.notesearch.NoteSearchViewModel
import com.originb.inkwisenote2.modules.queries.data.QueryRepository
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // 1. Single instance of the Database
    single {
        Room.databaseBuilder(get(), NotesDatabase::class.java, "NoteText.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    // 2. Single instances of DAOs (optional, but very helpful)
    single { get<NotesDatabase>().atomicNoteEntitiesDao() }
    single { get<NotesDatabase>().noteTermFrequencyDao() }
    single { get<NotesDatabase>().noteOcrTextDao() }
    single { get<NotesDatabase>().smartBooksDao() }
    single { get<NotesDatabase>().smartBookPagesDao() }
    single { get<NotesDatabase>().handwrittenNotesDao() }

    // 3. Single instances of Repositories
    single { HandwrittenNoteRepository() }
    single { NoteRelationRepository() }
    single { AtomicNotesDomain() }
    single { QueryRepository() }
    single { SmartNotebookRepository() }

    viewModel {
        NoteSearchViewModel(
            get(),
            get()
        )
    }
    viewModel {
        AdminViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}