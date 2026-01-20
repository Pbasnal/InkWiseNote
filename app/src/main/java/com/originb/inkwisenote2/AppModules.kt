package com.originb.inkwisenote2

import androidx.room.Room
import com.originb.inkwisenote2.common.NotesDatabase
import com.originb.inkwisenote2.modules.admin.AdminViewModel
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.noterelation.ui.RelatedNotesViewModel
import com.originb.inkwisenote2.modules.notesearch.NoteSearchViewModel
import com.originb.inkwisenote2.modules.queries.data.QueryRepository
import com.originb.inkwisenote2.modules.queries.ui.QueryViewModel
import com.originb.inkwisenote2.modules.queries.ui.QueryResultsViewModel
import com.originb.inkwisenote2.modules.noterelation.service.NoteTfIdfLogic
import com.originb.inkwisenote2.modules.noterelation.worker.NoteRelationWorker
import com.originb.inkwisenote2.modules.noterelation.worker.TextProcessingWorker
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain
import com.originb.inkwisenote2.modules.repositories.NoteRelationRepository
import com.originb.inkwisenote2.modules.repositories.SmartNotebookRepository
import com.originb.inkwisenote2.modules.smarthome.SmartHomePageViewModel
import com.originb.inkwisenote2.modules.handwrittennotes.HandwrittenNoteEventListener
import com.originb.inkwisenote2.modules.smartnotes.SmartNotebookEventListener
import com.originb.inkwisenote2.modules.noterelation.NoteRelationEventListener
import com.originb.inkwisenote2.modules.ocr.worker.NoteOcrEventListener
import com.originb.inkwisenote2.modules.textnote.TextNoteListener
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
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
    single { get<NotesDatabase>().queryDao() }

    // 3. Single instances of Repositories
    single { HandwrittenNoteRepository(get(), get()) }
    single { NoteRelationRepository() }
    single { AtomicNotesDomain(get()) }
    single { QueryRepository(get()) }
    single { SmartNotebookRepository() }

    // 3.5. Single instances of Services
    single { NoteTfIdfLogic(get()) }

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
    viewModel { QueryViewModel(get(), get()) }  // Application, QueryRepository
    viewModel { SmartHomePageViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { QueryResultsViewModel(get(), get()) }  // QueryRepository, SmartHomePageViewModel

    // 5. Workers
    worker {
        NoteRelationWorker(
            get(),
            get(),
            get(),  // NoteTfIdfLogic
            get(),  // NoteTermFrequencyDao
            get(),  // NoteRelationDao
            get(),  // SmartNotebookRepository
            get(),  // SmartBookPagesDao
            get()   // AtomicNotesDomain
        )
    }
    worker {
        TextProcessingWorker(
            get(),
            get(),
            get(),  // NoteTfIdfLogic
            get(),  // NoteOcrTextDao
            get(),  // TextNotesDao
            get(),  // AtomicNotesDomain
            get()   // SmartNotebookRepository
        )
    }
}