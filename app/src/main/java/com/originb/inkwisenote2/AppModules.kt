package com.originb.inkwisenote2

import androidx.room.Room
import com.originb.inkwisenote2.common.NotesDatabase
import com.originb.inkwisenote2.modules.admin.AdminViewModel
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.noterelation.ui.RelatedNotesViewModel
import com.originb.inkwisenote2.modules.notesearch.NoteSearchViewModel
import com.originb.inkwisenote2.modules.smartnotes.viewmodels.SmartNotebookViewModel
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
import com.originb.inkwisenote2.modules.smartnotes.ui.TextNoteFragment
import com.originb.inkwisenote2.modules.smartnotes.ui.HandwrittenNoteFragment
import com.originb.inkwisenote2.modules.smartnotes.ui.InitNoteFragment
import org.koin.androidx.fragment.dsl.fragment
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.parameter.parametersOf
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
    single { NoteRelationRepository(get(), get(), get()) }
    single { AtomicNotesDomain(get()) }
    single { QueryRepository(get()) }
    single { SmartNotebookRepository(get(), get(), get(), get()) }

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
        SmartNotebookViewModel(
            get(),
            get(),
            get(),
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

    // 4.5. Fragment factories
    fragment { (smartNotebook: com.originb.inkwisenote2.modules.repositories.SmartNotebook,
                atomicNote: com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity) ->
        TextNoteFragment(smartNotebook, atomicNote, get(), get(), get(), get())
    }
    fragment { (smartNotebook: com.originb.inkwisenote2.modules.repositories.SmartNotebook,
                atomicNote: com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity) ->
        HandwrittenNoteFragment(smartNotebook, atomicNote, get(), get(), get(), get())
    }
    fragment { (smartNotebook: com.originb.inkwisenote2.modules.repositories.SmartNotebook,
                atomicNote: com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity,
                adapter: com.originb.inkwisenote2.modules.smartnotes.ui.SmartNotebookAdapter) ->
        InitNoteFragment(smartNotebook, atomicNote, adapter)
    }

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
            get(),  // NoteOcrTextsDao
            get(),  // TextNotesDao
            get(),  // AtomicNotesDomain
            get()   // SmartNotebookRepository
        )
    }
}