package com.originb.inkwisenote2.di

import org.basnalcorp.shared.AppSecrets
import org.basnalcorp.shared.BackgroundScheduler
import org.basnalcorp.shared.PlatformLogger
import org.basnalcorp.shared.createDriver
import org.basnalcorp.shared.db.NotesDatabase
import org.koin.dsl.module

/**
 * Provides shared actuals and SQLDelight database for the shared module.
 * Load this before [org.basnalcorp.shared.di.sharedModule] so that
 * [NotesDatabase], [PlatformLogger], [AppSecrets], [BackgroundScheduler] are available.
 */
val sharedActualsModule = module {
    single { createDriver() }
    single { NotesDatabase(get()) }
    single { PlatformLogger() }
    single { AppSecrets() }
    single { BackgroundScheduler() }
}
