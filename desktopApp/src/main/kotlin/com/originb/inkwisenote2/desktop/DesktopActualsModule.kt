package com.originb.inkwisenote2.desktop

import org.basnalcorp.shared.AppSecrets
import org.basnalcorp.shared.BackgroundScheduler
import org.basnalcorp.shared.PlatformLogger
import org.basnalcorp.shared.createDriver
import org.basnalcorp.shared.db.NotesDatabase
import org.koin.dsl.module

/**
 * Phase 8.1: Koin module providing jvmMain actuals for desktop.
 * Mirrors [com.originb.inkwisenote2.di.SharedActualsModule] on Android.
 */
val desktopActualsModule = module {
    single { createDriver() }
    single { NotesDatabase(get()) }
    single { PlatformLogger() }
    single { AppSecrets() }
    single { BackgroundScheduler() }
}
