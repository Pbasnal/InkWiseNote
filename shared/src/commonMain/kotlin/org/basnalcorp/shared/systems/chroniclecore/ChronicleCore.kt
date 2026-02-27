package org.basnalcorp.shared.systems.chroniclecore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ChronicleCore is a self-contained actor-style system: a single-threaded command processor
 * with ordered execution and strong consistency. All mutations and reads go through [dispatch].
 * No direct file or DB writes from outside; no public create/update/delete functions.
 *
 * [db] and [fileSystem] are injected so the system remains independent of concrete storage.
 * [notesRoot] is the root directory for all Chronicle data (e.g. "{appStorageRoot()}/notes").
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

    /**
     * Single public entry point for mutations and reads. Commands are processed sequentially.
     */
    suspend fun dispatch(command: ChronicleCommand) {
        commandChannel.send(command)
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

    private fun notebookPath(notebookId: String): String = "$notesRoot/$notebookId"
    private fun noteFilePath(notebookId: String, noteId: Long): String = "$notesRoot/$notebookId/$noteId.md"

    private suspend fun emit(event: ChronicleEvent) {
        _events.emit(event)
    }

    private fun completeReplyOnError(command: ChronicleCommand, e: Exception) {
        when (command) {
            is ChronicleCommand.ListNotebooks -> command.reply.completeExceptionally(e)
            is ChronicleCommand.ListNotes -> command.reply.completeExceptionally(e)
            is ChronicleCommand.GetNote -> command.reply.complete(null)
            else -> { /* mutations have no reply */ }
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
            is ChronicleCommand.ListNotebooks -> handleListNotebooks(command)
            is ChronicleCommand.ListNotes -> handleListNotes(command)
            is ChronicleCommand.GetNote -> handleGetNote(command)
        }
    }

    private suspend fun handleCreateNotebook(command: ChronicleCommand.CreateNotebook) {
        val now = Clock.System.now().toEpochMilliseconds()
        val notebookId = command.name?.trim()?.ifBlank { null } ?: now.toString()
        val displayName = command.name?.trim()?.ifBlank { null } ?: notebookId
        val dirPath = notebookPath(notebookId)
        fileSystem.createDirectory(notesRoot)
        if (!fileSystem.createDirectory(dirPath)) return
        try {
            db.insertNotebook(notebookId, displayName, now)
            emit(ChronicleEvent.NotebookCreated(notebookId = notebookId, displayName = displayName, creationTime = now))
        } catch (e: Exception) {
            fileSystem.deleteDirectory(dirPath)
            throw e
        }
    }

    private suspend fun handleRenameNotebook(command: ChronicleCommand.RenameNotebook) {
        db.updateNotebookDisplayName(command.notebookId, command.newDisplayName)
        emit(ChronicleEvent.NotebookRenamed(notebookId = command.notebookId, newDisplayName = command.newDisplayName))
    }

    private suspend fun handleDeleteNotebook(command: ChronicleCommand.DeleteNotebook) {
        val dirPath = notebookPath(command.notebookId)
        db.deleteNotesForNotebook(command.notebookId)
        db.deleteNotebook(command.notebookId)
        fileSystem.deleteDirectory(dirPath)
        emit(ChronicleEvent.NotebookDeleted(notebookId = command.notebookId))
    }

    private suspend fun handleCreateNote(command: ChronicleCommand.CreateNote) {
        // 1) Validate notebook exists
        val notebookExists = db.getNotebook(command.notebookId) != null
        if (!notebookExists) throw IllegalStateException("Notebook ${command.notebookId} does not exist")

        // 2) creation_time from frontmatter allowed; if missing, generate. last_modified from frontmatter rejected.
        val creationTime = command.optionalFrontmatter?.get("creation_time")?.toLongOrNull()
            ?: Clock.System.now().toEpochMilliseconds()
        val noteId = creationTime

        // 3) File collision check: noteId must not already exist
        val filePath = noteFilePath(command.notebookId, noteId)
        if (fileSystem.exists(filePath)) throw IllegalStateException("Note file already exists: $noteId")

        // 4) Unknown frontmatter keys preserved; never use last_modified from input
        val unknownKeys = command.optionalFrontmatter
            ?.filter { it.key !in setOf("creation_time", "last_modified", "title") }
            ?: emptyMap()

        val lastModified = creationTime
        val content = Frontmatter.serialize(
            creationTime = creationTime,
            lastModified = lastModified,
            title = command.title,
            unknownKeys = unknownKeys,
            body = command.body
        )
        fileSystem.writeTextFile(filePath, content)
        try {
            db.insertNote(noteId, command.notebookId, command.title, creationTime, lastModified, filePath)
            emit(ChronicleEvent.NoteCreated(noteId = noteId, notebookId = command.notebookId, creationTime = creationTime, lastModified = lastModified))
        } catch (e: Exception) {
            fileSystem.deleteFile(filePath)
            throw e
        }
    }

    private suspend fun handleUpdateNote(command: ChronicleCommand.UpdateNote) {
        val filePath = noteFilePath(command.notebookId, command.noteId)
        val raw = fileSystem.readTextFile(filePath)
        val parsed = Frontmatter.parse(raw)
        if (parsed.lastModified != command.expectedLastModified) {
            throw IllegalStateException("Note ${command.noteId} last_modified mismatch: expected ${command.expectedLastModified}, got ${parsed.lastModified}")
        }
        val now = Clock.System.now().toEpochMilliseconds()
        val content = Frontmatter.serialize(
            creationTime = parsed.creationTime,
            lastModified = now,
            title = command.updatedTitle,
            unknownKeys = if (command.preserveUnknownKeys.isNotEmpty()) command.preserveUnknownKeys else parsed.unknownKeys,
            body = command.updatedBody
        )
        fileSystem.writeTextFile(filePath, content)
        db.updateNote(command.noteId, command.updatedTitle, now, filePath)
        emit(ChronicleEvent.NoteUpdated(noteId = command.noteId, notebookId = command.notebookId, lastModified = now))
    }

    private suspend fun handleDeleteNote(command: ChronicleCommand.DeleteNote) {
        val filePath = noteFilePath(command.notebookId, command.noteId)
        val raw = fileSystem.readTextFile(filePath)
        val parsed = Frontmatter.parse(raw)
        if (parsed.lastModified != command.expectedLastModified) {
            throw IllegalStateException("Note ${command.noteId} last_modified mismatch: expected ${command.expectedLastModified}, got ${parsed.lastModified}")
        }
        fileSystem.deleteFile(filePath)
        db.deleteNote(command.noteId)
        emit(ChronicleEvent.NoteDeleted(noteId = command.noteId, notebookId = command.notebookId, lastModified = command.expectedLastModified))
    }

    private fun handleListNotebooks(command: ChronicleCommand.ListNotebooks) {
        command.reply.complete(db.listNotebooks())
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
}
