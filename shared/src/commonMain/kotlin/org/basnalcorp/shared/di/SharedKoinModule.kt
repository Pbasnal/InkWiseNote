package org.basnalcorp.shared.di

import org.basnalcorp.shared.data.repository.AtomicNotesRepository
import org.basnalcorp.shared.data.repository.NoteTermFrequencyRepository
import org.basnalcorp.shared.data.repository.QueryRepository
import org.basnalcorp.shared.data.repository.SmartBookPagesRepository
import org.basnalcorp.shared.data.repository.SmartBooksRepository
import org.basnalcorp.shared.data.repository.SmartNotebookRepository
import org.basnalcorp.shared.data.repository.TextNotesRepository
import org.basnalcorp.shared.data.repository.impl.AtomicNotesRepositoryImpl
import org.basnalcorp.shared.data.repository.impl.TextNotesRepositoryImpl
import org.basnalcorp.shared.data.repository.impl.NoteTermFrequencyRepositoryImpl
import org.basnalcorp.shared.data.repository.impl.QueryRepositoryImpl
import org.basnalcorp.shared.data.repository.impl.SmartBookPagesRepositoryImpl
import org.basnalcorp.shared.data.repository.impl.SmartBooksRepositoryImpl
import org.basnalcorp.shared.data.repository.impl.SmartNotebookRepositoryImpl
import org.basnalcorp.shared.db.NotesDatabase
import org.basnalcorp.shared.state.FileExplorerStateHolder
import org.basnalcorp.shared.state.NotebookListStateHolder
import org.basnalcorp.shared.state.RelatedNotesStateHolder
import org.basnalcorp.shared.state.QueryListStateHolder
import org.basnalcorp.shared.state.NoteDetailStateHolder
import org.basnalcorp.shared.state.SmartNotebookStateHolder
import org.basnalcorp.shared.tfidf.NoteTfIdfLogic
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCore
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for shared repositories and state holders.
 * Requires the host (Android/Desktop) to provide: [NotesDatabase], [org.basnalcorp.shared.PlatformLogger].
 * The app adds an android-specific module that provides actuals + [NotesDatabase] from [org.basnalcorp.shared.createDriver].
 */
fun sharedModule(): Module = module {
    single { AtomicNotesRepositoryImpl(get()) }.bind<AtomicNotesRepository>()
    single { TextNotesRepositoryImpl(get()) }.bind<TextNotesRepository>()
    single { SmartBooksRepositoryImpl(get()) }.bind<SmartBooksRepository>()
    single { SmartBookPagesRepositoryImpl(get()) }.bind<SmartBookPagesRepository>()
    single { NoteTermFrequencyRepositoryImpl(get()) }.bind<NoteTermFrequencyRepository>()
    single { QueryRepositoryImpl(get()) }.bind<QueryRepository>()
    single {
        SmartNotebookRepositoryImpl(
            get<AtomicNotesRepository>(),
            get<SmartBooksRepository>(),
            get<SmartBookPagesRepository>(),
            get<TextNotesRepository>()
        )
    }.bind<SmartNotebookRepository>()
    singleOf(::NoteTfIdfLogic)
    single { NotebookListStateHolder(get(), try { get<ChronicleCore>() } catch (_: Exception) { null }) }
    singleOf(::QueryListStateHolder)
    singleOf(::SmartNotebookStateHolder)
    singleOf(::NoteDetailStateHolder)
    singleOf(::FileExplorerStateHolder)
    singleOf(::RelatedNotesStateHolder)
}
