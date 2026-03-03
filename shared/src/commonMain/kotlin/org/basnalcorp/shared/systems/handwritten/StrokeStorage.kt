package org.basnalcorp.shared.systems.handwritten

import org.basnalcorp.shared.systems.chroniclecore.ChronicleFileSystem

/**
 * Stroke file I/O and content hash. Uses [ChronicleFileSystem] for read/write so the
 * drawing system stays testable and format lives in one place.
 */
object StrokeStorage {

    /**
     * Loads strokes from the file at [filePath]. Returns empty list if file does not exist or is invalid.
     */
    fun loadStrokes(fileSystem: ChronicleFileSystem, filePath: String): List<Stroke> {
        return try {
            val content = fileSystem.readTextFile(filePath)
            StrokeJson.decode(content)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Saves [strokes] to the file at [filePath]. Overwrites existing file. Parent directory must exist.
     */
    fun saveStrokes(fileSystem: ChronicleFileSystem, filePath: String, strokes: List<Stroke>) {
        val content = StrokeJson.encode(strokes)
        fileSystem.writeTextFile(filePath, content)
    }

    /**
     * Returns a stable hash string for [strokes] for dirty checking (deterministic from JSON content).
     */
    fun strokesHash(strokes: List<Stroke>): String {
        val json = StrokeJson.encode(strokes)
        var h = 0L
        for (c in json) {
            h = (h * 31 + c.code) and 0xFFFFFFFFL
        }
        return h.toString(16)
    }
}
