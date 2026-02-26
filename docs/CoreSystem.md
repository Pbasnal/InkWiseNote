ChronicleCore – Core System Specification v1.1
1. Purpose

ChronicleCore is the single authoritative system responsible for:

Managing notebooks and notes

Persisting notes as Markdown files

Maintaining a strongly consistent Index Database

Emitting ordered in-process domain events

Enforcing all core invariants

The filesystem is the source of truth.
The Index Database is a derived, strongly consistent read model.

All state mutations MUST pass through ChronicleCore.

2. Storage Model
2.1 Folder Structure
./notes/
    /<notebookId>/
        /<noteId>.md

notebookId is the folder name.

If user provides a notebook name → use that exact string.

If user provides no name → use current timestamp (epoch millis) as notebookId.

Notebook rename:

Renames folder only.

Does NOT modify noteIds.

Does NOT modify note contents.

Does NOT modify note frontmatter.

3. Identity Model
3.1 noteId

noteId is always the note’s creation time in epoch milliseconds.

Rules:

On note creation:

If frontmatter contains creation_time, use it as noteId.

If frontmatter does NOT contain creation_time, generate current epoch millis.

Persist that value into frontmatter as creation_time.

noteId is immutable.

Filename MUST equal <noteId>.md.

Uniqueness Assumptions:

Single-process runtime.

No bulk simultaneous note creation.

Single-threaded command execution.

3.2 Notebook Identity

notebookId equals folder name.

Immutable after creation except via explicit rename command.

Notebook rename changes only folder name.

4. Note File Format

Each note file MUST follow:

---
<frontmatter>
---

<markdown body>

Frontmatter is YAML.

4.1 Required Core Properties

creation_time (epoch millis, immutable)

last_modified (epoch millis)

title (string)

4.2 Core Rules

creation_time MUST match filename (noteId).

If mismatch is detected during load:

Filename value is authoritative.

Frontmatter MUST be corrected.

last_modified MUST be updated on every successful write.

Core validates only its own properties.

Unknown frontmatter keys MUST be preserved exactly.

Unknown keys MUST NOT be removed unless explicitly deleted via command.

5. Index Database

The Index Database is a strongly consistent read model.

5.1 Notes Table

Must store:

noteId

notebookId

title

creationTime

lastModified

filePath

5.2 Notebooks Table

Must store:
notebookId

creationTime

5.3 Consistency Guarantee

Filesystem write and DB update MUST occur within the same event loop transaction.

No partial commits allowed.

If file write fails → DB MUST NOT update.

If DB update fails → file write MUST be rolled back if possible, otherwise system enters recovery-required state.

6. Command API (Single Write Entry Point)

All mutations MUST go through the command API.

Direct file writes are forbidden.

6.1 Notebook Commands
createNotebook(name?)

If name is null or empty → use current epoch millis as notebookId.

Create folder.

Insert notebook into DB.

Emit NotebookCreated.

renameNotebook(notebookId, newName)

Rename folder.

Update DB.

Emit NotebookRenamed.

deleteNotebook(notebookId)

Delete folder recursively.

Remove related notes from DB.

Emit NotebookDeleted.

6.2 Note Commands
createNote(notebookId, title, body, optionalFrontmatter?)

Steps:

Determine creation_time:

If provided in frontmatter → use it.

Else → generate current epoch millis.

noteId = creation_time.

Ensure no file with same noteId exists.

Construct frontmatter:

creation_time

last_modified = creation_time

title

merge unknown keys (if provided)

Write file <noteId>.md.

Insert into DB.

Emit NoteCreated.

updateNote(noteId, updatedContent, expectedLastModified)

Steps:

Load file from disk.

Parse frontmatter and body.

Verify last_modified == expectedLastModified.

If mismatch → reject command.

Preserve:

creation_time

Unknown keys (unless explicitly removed)

Update:

title (if changed)

body

last_modified = current epoch millis

Write file.

Update DB.

Emit NoteUpdated.

Optimistic concurrency is mandatory.

deleteNote(noteId, expectedLastModified)

Load file.

Verify last_modified matches.

Delete file.

Remove from DB.

Emit NoteDeleted (must include last_modified).

7. Event Model

All events are:

Emitted after successful commit.

Ordered.

Delivered synchronously within same process.

At-most-once.

Not persisted.

Event types:

NotebookCreated

NotebookRenamed

NotebookDeleted

NoteCreated

NoteUpdated

NoteDeleted

Event payload MUST include:

noteId (if applicable)

notebookId

lastModified (for note events)

8. Read Model

Note loading behavior:

Always read from disk.

Parse frontmatter.

Return full note object.

No in-memory note cache.

OS-level file caching is relied upon.

9. Crash Recovery

On startup:

Scan entire filesystem.

For each notebook folder:

Validate existence in DB.

For each note file:

Parse frontmatter.

If creation_time missing:

Use filename as creation_time.

Update frontmatter.

If mismatch between filename and frontmatter:

Filename is authoritative.

Rewrite frontmatter.

Rebuild DB from filesystem.

No events emitted during rebuild.

Filesystem state always overrides DB state.

10. Frontmatter Boundaries

Core System Responsibilities:

Validate core properties.

Preserve unknown properties.

External Systems:

May add frontmatter keys.

May remove their own keys.

May modify core properties only through command API.

Direct file edits by external systems are unsupported and may be corrected during recovery.

11. Invariants

noteId == creation_time.

Filename == noteId.

noteId is immutable.

Notebook rename does not affect notes.

Filesystem is ultimate authority.

All writes go through command API.

Unknown frontmatter keys are preserved.

DB is strongly consistent with filesystem at runtime.

Event order equals command execution order.