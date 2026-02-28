package org.basnalcorp.shared.systems.markdownnote

import org.basnalcorp.shared.systems.chroniclecore.ChronicleNoteContent
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownNoteContentTest {

    @Test
    fun fromChronicleContent_usesNoteTypeFromFrontmatter() {
        val content = ChronicleNoteContent(
            noteId = 1L,
            notebookId = "nb1",
            title = "Title",
            creationTime = 100L,
            lastModified = 200L,
            body = "body",
            unknownFrontmatter = mapOf("note_type" to "markdown")
        )
        val result = MarkdownNoteContent.fromChronicleContent(content)
        assertEquals(1L, result.noteId)
        assertEquals("nb1", result.notebookId)
        assertEquals("Title", result.title)
        assertEquals("body", result.body)
        assertEquals("markdown", result.noteType)
        assertEquals(100L, result.creationTime)
        assertEquals(200L, result.lastModified)
    }

    @Test
    fun fromChronicleContent_defaultsNoteTypeWhenMissing() {
        val content = ChronicleNoteContent(
            noteId = 2L,
            notebookId = "nb2",
            title = "T",
            creationTime = 10L,
            lastModified = 20L,
            body = "b",
            unknownFrontmatter = emptyMap()
        )
        val result = MarkdownNoteContent.fromChronicleContent(content)
        assertEquals("markdown", result.noteType)
    }
}
