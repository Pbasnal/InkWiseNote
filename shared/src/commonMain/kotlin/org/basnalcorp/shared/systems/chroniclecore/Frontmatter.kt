package org.basnalcorp.shared.systems.chroniclecore

/**
 * Minimal frontmatter parser/serializer for ChronicleCore note files.
 * Required keys: creation_time (Long), last_modified (Long), title (String).
 * Unknown keys are preserved on parse and written back on serialize.
 */
object Frontmatter {

    private const val DELIM = "---"

    data class Parsed(
        val creationTime: Long,
        val lastModified: Long,
        val title: String,
        val unknownKeys: Map<String, String>,
        val body: String
    )

    /**
     * Splits content by "---", parses first block as YAML-like key: value, rest as body.
     * Required keys: creation_time, last_modified, title.
     */
    fun parse(content: String): Parsed {
        val parts = content.split(DELIM, limit = 3)
        val frontmatterBlock = parts.getOrNull(1)?.trim() ?: ""
        val body = parts.getOrNull(2)?.trimStart() ?: ""

        var creationTime = 0L
        var lastModified = 0L
        var title = ""
        val unknownKeys = mutableMapOf<String, String>()

        for (line in frontmatterBlock.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val colonIdx = trimmed.indexOf(':')
            if (colonIdx <= 0) continue
            val key = trimmed.substring(0, colonIdx).trim()
            val value = trimmed.substring(colonIdx + 1).trim().removeSurrounding("\"", "\"").removeSurrounding("'", "'")
            when (key) {
                "creation_time" -> creationTime = value.toLongOrNull() ?: 0L
                "last_modified" -> lastModified = value.toLongOrNull() ?: 0L
                "title" -> title = value
                else -> unknownKeys[key] = value
            }
        }

        return Parsed(
            creationTime = creationTime,
            lastModified = lastModified,
            title = title,
            unknownKeys = unknownKeys,
            body = body
        )
    }

    /**
     * Serializes to "---\nkey: value\n...\n---\n\nbody".
     */
    fun serialize(
        creationTime: Long,
        lastModified: Long,
        title: String,
        unknownKeys: Map<String, String> = emptyMap(),
        body: String
    ): String {
        val lines = mutableListOf<String>()
        lines.add("creation_time: $creationTime")
        lines.add("last_modified: $lastModified")
        lines.add("title: \"${title.replace("\"", "\\\"")}\"")
        unknownKeys.forEach { (k, v) ->
            lines.add("$k: \"${v.replace("\"", "\\\"")}\"")
        }
        return "$DELIM\n${lines.joinToString("\n")}\n$DELIM\n\n$body"
    }
}
