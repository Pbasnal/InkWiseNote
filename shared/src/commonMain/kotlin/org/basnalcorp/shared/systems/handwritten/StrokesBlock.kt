package org.basnalcorp.shared.systems.handwritten

/**
 * Helpers for the strokes code block in markdown body.
 * Convention: body may contain a fenced code block with language `strokes` and a single line: the strokes file name (e.g. `<noteId>.strokes.json`).
 */

private const val STROKES_FENCE = "```strokes"
private val STROKES_FENCE_END = Regex("```\\s*")

/**
 * Parses the body for a ```strokes code block and returns the first line (strokes file path), or null if not found.
 */
fun parseStrokesBlock(body: String): String? {
    val idx = body.indexOf(STROKES_FENCE)
    if (idx < 0) return null
    val afterFence = body.indexOf('\n', idx)
    if (afterFence < 0) return null
    val lineStart = afterFence + 1
    val lineEnd = body.indexOf('\n', lineStart).let { if (it < 0) body.length else it }
    val line = body.substring(lineStart, lineEnd).trim()
    if (line.isEmpty()) return null
    val endFence = body.indexOf("```", lineEnd)
    if (endFence < 0) return null
    return line
}

/**
 * Inserts or replaces the ```strokes code block in [body] with a block containing [strokesPath].
 * Preserves the rest of the body. If no existing block, appends the block.
 */
fun setStrokesBlockInBody(body: String, strokesPath: String): String {
    val block = "\n$STROKES_FENCE\n$strokesPath\n```\n"
    val idx = body.indexOf(STROKES_FENCE)
    if (idx < 0) {
        val trimmed = body.trimEnd()
        return if (trimmed.isEmpty()) block.trimStart() else "$trimmed$block"
    }
    val afterFence = body.indexOf('\n', idx)
    if (afterFence < 0) return body + block
    val nextLineStart = afterFence + 1
    val lineEnd = body.indexOf('\n', nextLineStart).let { if (it < 0) body.length else it }
    val endFence = body.indexOf("```", lineEnd)
    val endOfBlock = if (endFence < 0) lineEnd else body.indexOf('\n', endFence).let { if (it < 0) body.length else it + 1 }
    val before = body.substring(0, idx).trimEnd()
    val after = body.substring(endOfBlock).trimStart()
    return buildString {
        if (before.isNotEmpty()) append(before).append("\n")
        append(block.trimStart())
        if (after.isNotEmpty()) append("\n").append(after)
    }
}
