package com.originb.inkwisenote2

import androidx.room.Room
import com.originb.inkwisenote2.common.NotesDatabase
import com.originb.inkwisenote2.modules.admin.AdminViewModel
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.noterelation.ui.RelatedNotesViewModel
import com.originb.inkwisenote2.modules.notesearch.NoteSearchViewModel
import com.originb.inkwisenote2.modules.queries.data.QueryRepository
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.handwrittennotes.HandwrittenNoteEventListener
import com.originb.inkwisenote2.modules.smartnotes.SmartNotebookEventListener
import com.originb.inkwisenote2.modules.noterelation.NoteRelationEventListener
import com.originb.inkwisenote2.modules.ocr.worker.NoteOcrEventListener
import com.originb.inkwisenote2.modules.textnote.TextNoteListener
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
    single { get<NotesDatabase>().noteRelationDao() }
    single { get<NotesDatabase>().textNotesDao() }

    // 3. Single instances of Repositories
    single { HandwrittenNoteRepository(get(), get()) }
    single { NoteRelationRepository() }
    single { AtomicNotesDomain(get()) }
    single { QueryRepository() }
    single { SmartNotebookRepository() }

    // 4. Single instances of Event Listeners
    single { HandwrittenNoteEventListener(get()) }
    single { SmartNotebookEventListener(get()) }
    single { NoteRelationEventListener(get()) }
    single { NoteOcrEventListener(get()) }
    single { TextNoteListener(get()) }

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
    viewModel {
        RelatedNotesViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
}