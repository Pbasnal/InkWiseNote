package org.basnalcorp.shared.systems.chroniclecore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ChronicleCore is a self-contained actor-style system: a single-threaded command processor
 * with ordered execution and strong consistency. FS is source of truth; all mutations use
 * two-phase semantics (FS step then DB step). No separate sync-only commands.
 */
class ChronicleCore(
    private val db: ChronicleCoreDb,
    private val fileSystem: ChronicleFileSystem,
    private val notesRoot: String,
    private val scope: CoroutineScope
) {

    private val commandChannel = Channel<ChronicleCommand>(Channel.UNLIMITED)
    private val _events = MutableSharedFlow<ChronicleEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val events: SharedFlow<ChronicleEvent> = _events

    init {
        scope.launch {
            for (command in commandChannel) {
                try {
                    handle(command)
                } catch (e: Exception) {
                    completeReplyOnError(command, e)
                }
            }
        }
    }

    suspend fun dispatch(command: ChronicleCommand) {
        commandChannel.send(command)
    }

    suspend fun createNotebook(name: String): ChronicleCommandResult<ChronicleNotebook> {
        val reply = CompletableDeferred<ChronicleCommandResult<ChronicleNotebook>>()
        dispatch(ChronicleCommand.CreateNotebook(name, reply))
        return reply.await()
    }

    suspend fun renameNotebook(notebookId: String, newNotebookId: String): ChronicleCommandResult<ChronicleNotebook> {
        val reply = CompletableDeferred<ChronicleCommandResult<ChronicleNotebook>>()
        dispatch(ChronicleCommand.RenameNotebook(notebookId, newNotebookId, reply))
        return reply.await()
    }

    suspend fun deleteNotebook(notebookId: String): ChronicleCommandResult<Unit> {
        val reply = CompletableDeferred<ChronicleCommandResult<Unit>>()
        dispatch(ChronicleCommand.DeleteNotebook(notebookId, reply))
        return reply.await()
    }

    suspend fun createNote(
        notebookId: String,
        title: String,
        body: String,
        optionalFrontmatter: Map<String, String> = emptyMap(),
        optionalNoteId: Long? = null
    ): ChronicleCommandResult<ChronicleNoteContent> {
        val reply = CompletableDeferred<ChronicleCommandResult<ChronicleNoteContent>>()
        dispatch(ChronicleCommand.CreateNote(notebookId, title, body, optionalFrontmatter, optionalNoteId, reply))
        return reply.await()
    }

    suspend fun updateNote(
        noteId: Long,
        notebookId: String,
        updatedTitle: String,
        updatedBody: String,
        expectedLastModified: Long,
        preserveUnknownKeys: Map<String, String> = emptyMap()
    ): ChronicleCommandResult<ChronicleNoteContent> {
        val reply = CompletableDeferred<ChronicleCommandResult<ChronicleNoteContent>>()
        dispatch(
            ChronicleCommand.UpdateNote(
                noteId, notebookId, updatedTitle, updatedBody,
                expectedLastModified, preserveUnknownKeys, reply
            )
        )
        return reply.await()
    }

    suspend fun deleteNote(
        noteId: Long,
        notebookId: String,
        expectedLastModified: Long
    ): ChronicleCommandResult<Unit> {
        val reply = CompletableDeferred<ChronicleCommandResult<Unit>>()
        dispatch(ChronicleCommand.DeleteNote(noteId, notebookId, expectedLastModified, reply))
        return reply.await()
    }

    suspend fun resync(notebookIds: List<String>): ReceiveChannel<SyncStatus> {
        val reply = CompletableDeferred<ReceiveChannel<SyncStatus>>()
        dispatch(ChronicleCommand.Resync(notebookIds, reply))
        return reply.await()
    }

    suspend fun listNotebooks(): List<ChronicleNotebook> {
        val reply = CompletableDeferred<List<ChronicleNotebook>>()
        dispatch(ChronicleCommand.ListNotebooks(reply))
        return reply.await()
    }

    suspend fun listNotes(notebookId: String): List<ChronicleNoteMeta> {
        val reply = CompletableDeferred<List<ChronicleNoteMeta>>()
        dispatch(ChronicleCommand.ListNotes(notebookId, reply))
        return reply.await()
    }

    suspend fun getNote(notebookId: String, noteId: Long): ChronicleNoteContent? {
        val reply = CompletableDeferred<ChronicleNoteContent?>()
        dispatch(ChronicleCommand.GetNote(notebookId, noteId, reply))
        return reply.await()
    }

    internal suspend fun getNotebook(notebookId: String): ChronicleNotebook? {
        val reply = CompletableDeferred<ChronicleNotebook?>()
        dispatch(ChronicleCommand.GetNotebook(notebookId, reply))
        return reply.await()
    }

    private fun notebookPath(notebookId: String): String = "$notesRoot/$notebookId"
    private fun noteFilePath(notebookId: String, noteId: Long): String = "$notesRoot/$notebookId/$noteId.md"

    private suspend fun emit(event: ChronicleEvent) {
        _events.emit(event)
    }

    private suspend fun emitFailure(operation: String, message: String) {
        _events.emit(ChronicleEvent.OperationFailed(operation = operation, message = message))
    }

    private suspend fun completeReplyOnError(command: ChronicleCommand, e: Exception) {
        val message = e.message ?: "unknown error"
        val operation = when (command) {
            is ChronicleCommand.CreateNotebook -> "CreateNotebook"
            is ChronicleCommand.RenameNotebook -> "RenameNotebook"
            is ChronicleCommand.DeleteNotebook -> "DeleteNotebook"
            is ChronicleCommand.CreateNote -> "CreateNote"
            is ChronicleCommand.UpdateNote -> "UpdateNote"
            is ChronicleCommand.DeleteNote -> "DeleteNote"
            else -> "ChronicleCore"
        }
        emitFailure(operation, message)
        when (command) {
            is ChronicleCommand.CreateNotebook -> command.reply.complete(ChronicleCommandResult.Failure(message))
            is ChronicleCommand.RenameNotebook -> command.reply.complete(ChronicleCommandResult.Failure(message))
            is ChronicleCommand.DeleteNotebook -> command.reply.complete(ChronicleCommandResult.Failure(message))
            is ChronicleCommand.CreateNote -> command.reply.complete(ChronicleCommandResult.Failure(message))
            is ChronicleCommand.UpdateNote -> command.reply.complete(ChronicleCommandResult.Failure(message))
            is ChronicleCommand.DeleteNote -> command.reply.complete(ChronicleCommandResult.Failure(message))
            is ChronicleCommand.Resync -> command.reply.completeExceptionally(e)
            is ChronicleCommand.GetNotebook -> command.reply.complete(null)
            is ChronicleCommand.ListNotebooks -> command.reply.completeExceptionally(e)
            is ChronicleCommand.ListNotes -> command.reply.completeExceptionally(e)
            is ChronicleCommand.GetNote -> command.reply.complete(null)
            else -> { /* no reply */ }
        }
    }

    private suspend fun handle(command: ChronicleCommand) {
        when (command) {
            is ChronicleCommand.CreateNotebook -> handleCreateNotebook(command)
            is ChronicleCommand.RenameNotebook -> handleRenameNotebook(command)
            is ChronicleCommand.DeleteNotebook -> handleDeleteNotebook(command)
            is ChronicleCommand.CreateNote -> handleCreateNote(command)
            is ChronicleCommand.UpdateNote -> handleUpdateNote(command)
            is ChronicleCommand.DeleteNote -> handleDeleteNote(command)
            is ChronicleCommand.Resync -> handleResync(command)
            is ChronicleCommand.ListNotebooks -> handleListNotebooks(command)
            is ChronicleCommand.ListNotes -> handleListNotes(command)
            is ChronicleCommand.GetNote -> handleGetNote(command)
            is ChronicleCommand.GetNotebook -> handleGetNotebook(command)
        }
    }

    private fun buildNotebookFromFs(notebookId: String): ChronicleNotebook? {
        val dirPath = notebookPath(notebookId)
        if (!fileSystem.exists(dirPath)) return null
        val noteIds = db.listNotes(notebookId).map { it.noteId }
        return ChronicleNotebook(notebookId = notebookId, noteIds = noteIds)
    }

    private suspend fun handleCreateNotebook(command: ChronicleCommand.CreateNotebook) {
        val notebookId = command.name.trim()
        if (notebookId.isBlank()) {
            emitFailure("CreateNotebook", "name must not be blank")
            command.reply.complete(ChronicleCommandResult.Failure("name must not be blank"))
            return
        }
        val dirPath = notebookPath(notebookId)
        // FS step: create directory if not exists
        fileSystem.createDirectory(notesRoot)
        val created = if (fileSystem.exists(dirPath)) false else fileSystem.createDirectory(dirPath)
        if (!created && !fileSystem.exists(dirPath)) {
            emitFailure("CreateNotebook", "failed to create directory")
            command.reply.complete(ChronicleCommandResult.Failure("failed to create directory"))
            return
        }
        // DB step: no notebook table; nothing to do
        if (created) {
            emit(ChronicleEvent.NotebookCreated(notebookId = notebookId))
        }
        val notebook = buildNotebookFromFs(notebookId)!!
        command.reply.complete(ChronicleCommandResult.Success(notebook))
    }

    private suspend fun handleRenameNotebook(command: ChronicleCommand.RenameNotebook) {
        val newId = command.newNotebookId.trim()
        if (newId.isBlank()) {
            emitFailure("RenameNotebook", "new notebook id must not be blank")
            command.reply.complete(ChronicleCommandResult.Failure("new notebook id must not be blank"))
            return
        }
        val oldPath = notebookPath(command.notebookId)
        val newPath = notebookPath(newId)
        // newId already exists on FS -> name conflict, no changes
        if (fileSystem.exists(newPath)) {
            emitFailure("RenameNotebook", "name conflict")
            command.reply.complete(ChronicleCommandResult.Failure("name conflict"))
            return
        }
        if (!fileSystem.exists(oldPath)) {
            // old doesn't exist; no-op (DB will be consistent after resync)
            emitFailure("RenameNotebook", "notebook not found")
            command.reply.complete(ChronicleCommandResult.Failure("notebook not found"))
            return
        }
        if (!fileSystem.renameDirectory(oldPath, newPath)) {
            emitFailure("RenameNotebook", "failed to rename directory")
            command.reply.complete(ChronicleCommandResult.Failure("failed to rename directory"))
            return
        }
        try {
            db.updateNotesNotebookId(newId, command.notebookId)
            emit(ChronicleEvent.NotebookRenamed(oldNotebookId = command.notebookId, newNotebookId = newId))
            val notebook = buildNotebookFromFs(newId)!!
            command.reply.complete(ChronicleCommandResult.Success(notebook))
        } catch (e: Exception) {
            fileSystem.renameDirectory(newPath, oldPath)
            val msg = e.message ?: "unknown error"
            emitFailure("RenameNotebook", msg)
            command.reply.complete(ChronicleCommandResult.Failure(msg))
        }
    }

    private suspend fun handleDeleteNotebook(command: ChronicleCommand.DeleteNotebook) {
        val dirPath = notebookPath(command.notebookId)
        // FS step: delete directory if exists
        val existed = fileSystem.exists(dirPath)
        if (existed) {
            if (!fileSystem.deleteDirectory(dirPath)) {
                emitFailure("DeleteNotebook", "failed to delete directory")
                command.reply.complete(ChronicleCommandResult.Failure("failed to delete directory"))
                return
            }
        }
        // DB step: sync with FS state (notebook doesn't exist on FS -> delete notes for it)
        db.deleteNotesForNotebook(command.notebookId)
        if (existed) {
            emit(ChronicleEvent.NotebookDeleted(notebookId = command.notebookId))
        }
        command.reply.complete(ChronicleCommandResult.Success(Unit))
    }

    private suspend fun handleCreateNote(command: ChronicleCommand.CreateNote) {
        val notebookPath = notebookPath(command.notebookId)
        if (!fileSystem.exists(notebookPath)) {
            emitFailure("CreateNote", "Notebook ${command.notebookId} does not exist")
            command.reply.complete(ChronicleCommandResult.Failure("Notebook ${command.notebookId} does not exist"))
            return
        }
        val isSyncPath = command.optionalNoteId != null
        if (isSyncPath) {
            // Sync path: file must exist; ensure DB row from file
            val noteId = command.optionalNoteId!!
            val filePath = noteFilePath(command.notebookId, noteId)
            if (!fileSystem.exists(filePath)) {
                emitFailure("CreateNote", "note file does not exist (sync)")
                command.reply.complete(ChronicleCommandResult.Failure("note file does not exist (sync)"))
                return
            }
            val raw = fileSystem.readTextFile(filePath)
            val parsed = Frontmatter.parse(raw)
            val dbMeta = db.getNote(noteId)
            val filePathResolved = noteFilePath(command.notebookId, noteId)
            if (dbMeta == null) {
                db.insertNote(noteId, command.notebookId, parsed.title, parsed.creationTime, parsed.lastModified, filePathResolved)
                emit(ChronicleEvent.NoteCreated(noteId = noteId, notebookId = command.notebookId, creationTime = parsed.creationTime, lastModified = parsed.lastModified))
            } else if (dbMeta.title != parsed.title || dbMeta.lastModified != parsed.lastModified) {
                db.updateNote(noteId, parsed.title, parsed.lastModified, filePathResolved)
                emit(ChronicleEvent.NoteUpdated(noteId = noteId, notebookId = command.notebookId, lastModified = parsed.lastModified))
            }
            val result = ChronicleNoteContent(
                noteId = noteId,
                notebookId = command.notebookId,
                title = parsed.title,
                creationTime = parsed.creationTime,
                lastModified = parsed.lastModified,
                body = parsed.body,
                unknownFrontmatter = parsed.unknownKeys
            )
            command.reply.complete(ChronicleCommandResult.Success(result))
            return
        }
        // User path: file must not exist
        val creationTime = command.optionalFrontmatter["creation_time"]?.toLongOrNull()
            ?: Clock.System.now().toEpochMilliseconds()
        val noteId = creationTime
        val filePath = noteFilePath(command.notebookId, noteId)
        if (fileSystem.exists(filePath)) {
            emitFailure("CreateNote", "note file already exists")
            command.reply.complete(ChronicleCommandResult.Failure("note file already exists"))
            return
        }
        val unknownKeys = command.optionalFrontmatter
            .filter { (k, _) -> k !in setOf("creation_time", "last_modified", "title") }
        val lastModified = creationTime
        val content = Frontmatter.serialize(
            creationTime = creationTime,
            lastModified = lastModified,
            title = command.title,
            unknownKeys = unknownKeys,
            body = command.body
        )
        try {
            fileSystem.writeTextFile(filePath, content)
            db.insertNote(noteId, command.notebookId, command.title, creationTime, lastModified, filePath)
            emit(ChronicleEvent.NoteCreated(noteId = noteId, notebookId = command.notebookId, creationTime = creationTime, lastModified = lastModified))
            val raw = fileSystem.readTextFile(filePath)
            val parsed = Frontmatter.parse(raw)
            val result = ChronicleNoteContent(
                noteId = noteId,
                notebookId = command.notebookId,
                title = parsed.title,
                creationTime = parsed.creationTime,
                lastModified = parsed.lastModified,
                body = parsed.body,
                unknownFrontmatter = parsed.unknownKeys
            )
            command.reply.complete(ChronicleCommandResult.Success(result))
        } catch (e: Exception) {
            if (fileSystem.exists(filePath)) fileSystem.deleteFile(filePath)
            val msg = e.message ?: "unknown error"
            emitFailure("CreateNote", msg)
            command.reply.complete(ChronicleCommandResult.Failure(msg))
        }
    }

    private suspend fun handleUpdateNote(command: ChronicleCommand.UpdateNote) {
        val filePath = noteFilePath(command.notebookId, command.noteId)
        val fsState = try {
            val raw = fileSystem.readTextFile(filePath)
            Frontmatter.parse(raw)
        } catch (e: Exception) {
            emitFailure("UpdateNote", e.message ?: "unknown error")
            command.reply.complete(ChronicleCommandResult.Failure(e.message ?: "unknown error"))
            return
        }
        if (fsState.lastModified != command.expectedLastModified) {
            val msg = "last_modified mismatch: expected ${command.expectedLastModified}, got ${fsState.lastModified}"
            emitFailure("UpdateNote", msg)
            command.reply.complete(ChronicleCommandResult.FailButRetry(msg))
            return
        }
        val now = Clock.System.now().toEpochMilliseconds()
        val content = Frontmatter.serialize(
            creationTime = fsState.creationTime,
            lastModified = now,
            title = command.updatedTitle,
            unknownKeys = command.preserveUnknownKeys.ifEmpty { fsState.unknownKeys },
            body = command.updatedBody
        )
        try {
            fileSystem.writeTextFile(filePath, content)
            db.updateNote(command.noteId, command.updatedTitle, now, filePath)
            emit(ChronicleEvent.NoteUpdated(noteId = command.noteId, notebookId = command.notebookId, lastModified = now))
            val result = ChronicleNoteContent(
                noteId = command.noteId,
                notebookId = command.notebookId,
                title = command.updatedTitle,
                creationTime = fsState.creationTime,
                lastModified = now,
                body = command.updatedBody,
                unknownFrontmatter = command.preserveUnknownKeys.ifEmpty { fsState.unknownKeys }
            )
            command.reply.complete(ChronicleCommandResult.Success(result))
        } catch (e: Exception) {
            val msg = e.message ?: "unknown error"
            emitFailure("UpdateNote", msg)
            command.reply.complete(ChronicleCommandResult.Failure(msg))
        }
    }

    private suspend fun handleDeleteNote(command: ChronicleCommand.DeleteNote) {
        val filePath = noteFilePath(command.notebookId, command.noteId)
        val fileExists = fileSystem.exists(filePath)
        if (fileExists) {
            val raw = fileSystem.readTextFile(filePath)
            val parsed = Frontmatter.parse(raw)
            if (parsed.lastModified != command.expectedLastModified) {
                val msg = "last_modified mismatch: expected ${command.expectedLastModified}, got ${parsed.lastModified}"
                emitFailure("DeleteNote", msg)
                command.reply.complete(ChronicleCommandResult.FailButRetry(msg))
                return
            }
            fileSystem.deleteFile(filePath)
        }
        db.deleteNote(command.noteId)
        if (fileExists) {
            emit(ChronicleEvent.NoteDeleted(noteId = command.noteId, notebookId = command.notebookId, lastModified = command.expectedLastModified))
        }
        command.reply.complete(ChronicleCommandResult.Success(Unit))
    }

    private fun handleResync(command: ChronicleCommand.Resync) {
        val channel = Channel<SyncStatus>(Channel.UNLIMITED)
        command.reply.complete(channel)
        scope.launch {
            try {
                runResync(command.notebookIds, channel)
            } catch (e: Exception) {
                channel.send(SyncStatus.Error(e.message ?: "unknown error"))
            } finally {
                channel.close()
            }
        }
    }

    private suspend fun runResync(notebookIds: List<String>, statusChannel: Channel<SyncStatus>) {
        statusChannel.send(SyncStatus.Idle)
        // Phase 1: Sync DB with FS (notebooks and notes on FS)
        val notebookList = notebookIds.ifEmpty {
            fileSystem.listDirectory(notesRoot)
                .filter { it.isDirectory }
                .map { it.name }
        }
        val totalNotebookCount = notebookList.size
        for ((index, notebookId) in notebookList.withIndex()) {
            statusChannel.send(
                SyncStatus.SyncingStatus(
                    notebookId = notebookId,
                    syncedNotebookCount = index,
                    totalNotebookCount = totalNotebookCount
                )
            )
            val notebookPath = this.notebookPath(notebookId)
            if (!fileSystem.exists(notebookPath)) continue
            reconcileNotesInNotebook(notebookId, notebookPath, statusChannel)
        }
        statusChannel.send(SyncStatus.Done)
        // Phase 2 (lower priority): Clean up DB — delete note rows for notebooks not on FS
        val dbNotebookIds = db.listNotebookIdsInDb()
        for (dbNotebookId in dbNotebookIds) {
            val inScope = notebookIds.isEmpty() || dbNotebookId in notebookIds
            if (!inScope) continue
            val folderPath = notebookPath(dbNotebookId)
            if (!fileSystem.exists(folderPath)) {
                db.deleteNotesForNotebook(dbNotebookId)
            }
        }
    }

    private suspend fun reconcileNotesInNotebook(
        notebookId: String,
        notebookPath: String,
        statusChannel: Channel<SyncStatus>
    ) {
        val noteFiles = fileSystem.listDirectory(notebookPath)
            .filter { !it.isDirectory && it.name.endsWith(".md") }
        val noteIdsFromFiles = noteFiles.mapNotNull { entry ->
            entry.name.removeSuffix(".md").toLongOrNull()
        }
        val dbNotes = listNotes(notebookId)
        val dbNotesById = dbNotes.associateBy { it.noteId }

        for (noteId in noteIdsFromFiles) {
            val content = getNote(notebookId, noteId) ?: continue
            val dbMeta = dbNotesById[noteId]
            when {
                dbMeta == null -> {
                    createNote(
                        notebookId = notebookId,
                        title = content.title,
                        body = content.body,
                        optionalFrontmatter = content.unknownFrontmatter + mapOf(
                            "creation_time" to content.creationTime.toString(),
                            "last_modified" to content.lastModified.toString()
                        ),
                        optionalNoteId = noteId
                    )
                }
                content.lastModified > dbMeta.lastModified -> {
                    var result = updateNote(
                        content.noteId,
                        notebookId,
                        content.title,
                        content.body,
                        content.lastModified,
                        content.unknownFrontmatter
                    )
                    var current = content
                    while (result is ChronicleCommandResult.FailButRetry) {
                        current = getNote(notebookId, noteId) ?: break
                        result = updateNote(
                            current.noteId,
                            notebookId,
                            current.title,
                            current.body,
                            current.lastModified,
                            current.unknownFrontmatter
                        )
                    }
                }
                dbMeta.lastModified > content.lastModified -> {
                    val current = getNote(notebookId, noteId) ?: continue
                    updateNote(
                        noteId,
                        notebookId,
                        dbMeta.title,
                        current.body,
                        current.lastModified,
                        current.unknownFrontmatter
                    )
                }
            }
        }

        for (meta in dbNotes) {
            val filePath = noteFilePath(notebookId, meta.noteId)
            if (!fileSystem.exists(filePath)) {
                deleteNote(meta.noteId, notebookId, meta.lastModified)
            }
        }
    }

    private fun handleListNotebooks(command: ChronicleCommand.ListNotebooks) {
        val list = fileSystem.listDirectory(notesRoot)
            .filter { it.isDirectory }
            .map { it.name }
            .map { id -> ChronicleNotebook(notebookId = id, noteIds = db.listNotes(id).map { it.noteId }) }
        command.reply.complete(list)
    }

    private fun handleListNotes(command: ChronicleCommand.ListNotes) {
        command.reply.complete(db.listNotes(command.notebookId))
    }

    private fun handleGetNote(command: ChronicleCommand.GetNote) {
        val filePath = noteFilePath(command.notebookId, command.noteId)
        val result = try {
            val raw = fileSystem.readTextFile(filePath)
            val parsed = Frontmatter.parse(raw)
            ChronicleNoteContent(
                noteId = command.noteId,
                notebookId = command.notebookId,
                title = parsed.title,
                creationTime = parsed.creationTime,
                lastModified = parsed.lastModified,
                body = parsed.body,
                unknownFrontmatter = parsed.unknownKeys
            )
        } catch (_: Exception) {
            null
        }
        command.reply.complete(result)
    }

    private fun handleGetNotebook(command: ChronicleCommand.GetNotebook) {
        command.reply.complete(buildNotebookFromFs(command.notebookId))
    }
}
