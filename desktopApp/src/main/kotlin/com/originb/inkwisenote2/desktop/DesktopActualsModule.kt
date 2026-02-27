package com.originb.inkwisenote2.desktop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.basnalcorp.shared.AppSecrets
import org.basnalcorp.shared.BackgroundScheduler
import org.basnalcorp.shared.PlatformLogger
import org.basnalcorp.shared.createDirectory
import org.basnalcorp.shared.deleteDirectory
import org.basnalcorp.shared.deleteFile
import org.basnalcorp.shared.listDirectory
import org.basnalcorp.shared.pathExists
import org.basnalcorp.shared.readTextFile
import org.basnalcorp.shared.renameDirectory
import org.basnalcorp.shared.writeTextFile
import org.basnalcorp.shared.appStorageRoot
import org.basnalcorp.shared.createDriver
import org.basnalcorp.shared.db.NotesDatabase
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCore
import org.basnalcorp.shared.systems.chroniclecore.ChronicleCoreDb
import org.basnalcorp.shared.systems.chroniclecore.ChronicleFileEntry
import org.basnalcorp.shared.systems.chroniclecore.ChronicleFileSystem
import org.basnalcorp.shared.systems.chroniclecore.impl.ChronicleCoreDbImpl
import org.basnalcorp.shared.systems.chroniclecore.impl.ChronicleFileSystemImpl
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

    single<ChronicleCoreDb> { ChronicleCoreDbImpl(get()) }
    single<ChronicleFileSystem> {
        ChronicleFileSystemImpl(
            readTextFile = { readTextFile(it) },
            writeTextFile = { path, content -> writeTextFile(path, content) },
            createDirectory = { createDirectory(it) },
            deleteFile = { deleteFile(it) },
            deleteDirectory = { deleteDirectory(it) },
            renameDirectoryFn = { a, b -> renameDirectory(a, b) },
            existsPath = { pathExists(it) },
            listDirectory = { path ->
                listDirectory(path).map { f ->
                    ChronicleFileEntry(path = f.path, name = f.name, isDirectory = f.isDirectory)
                }
            }
        )
    }
    single<ChronicleCore> {
        ChronicleCore(
            db = get(),
            fileSystem = get(),
            notesRoot = appStorageRoot() + "/notes",
            scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        )
    }
}
