package org.basnalcorp.shared.systems.handwritten

/**
 * Stable JSON format for strokes: color, width, points array with x, y, p, t.
 * Hand-written encoder/decoder for cross-platform use without kotlinx.serialization.
 */
object StrokeJson {

    private const val KEY_COLOR = "color"
    private const val KEY_WIDTH = "width"
    private const val KEY_POINTS = "points"
    private const val KEY_X = "x"
    private const val KEY_Y = "y"
    private const val KEY_P = "p"
    private const val KEY_T = "t"

    /**
     * Encodes [strokes] to a JSON array string. Format: [ { "color": long, "width": float, "points": [ {"x", "y", "p", "t"} ... ] }, ... ]
     */
    fun encode(strokes: List<Stroke>): String {
        if (strokes.isEmpty()) return "[]"
        return "[${strokes.joinToString(",") { encodeStroke(it) }}]"
    }

    private fun encodeStroke(s: Stroke): String {
        val pointsStr = s.points.joinToString(",") { p ->
            """{"$KEY_X":${p.x},"$KEY_Y":${p.y},"$KEY_P":${p.pressure},"$KEY_T":${p.timestamp}}"""
        }
        return """{"$KEY_COLOR":${s.color},"$KEY_WIDTH":${s.width},"$KEY_POINTS":[$pointsStr]}"""
    }

    /**
     * Decodes a JSON array string to a list of strokes. Returns empty list on parse error or empty array.
     */
    fun decode(json: String): List<Stroke> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return emptyList()
        if (!trimmed.startsWith("[")) return emptyList()
        val list = parseArray(trimmed, 1) ?: return emptyList()
        return list.mapNotNull { parseStroke(it) }
    }

    private fun parseArray(json: String, start: Int): List<String>? {
        var i = start
        if (i >= json.length || json[i] != '[') return null
        i++
        val result = mutableListOf<String>()
        while (i < json.length) {
            skipWhitespace(json, i).let { i = it }
            if (json[i] == ']') return result
            val elem = parseValue(json, i) ?: return null
            result.add(elem.first)
            i = elem.second
            skipWhitespace(json, i).let { i = it }
            if (i < json.length && json[i] == ',') i++
        }
        return result
    }

    private fun parseValue(json: String, start: Int): Pair<String, Int>? {
        var i = skipWhitespace(json, start)
        if (i >= json.length) return null
        when (json[i]) {
            '{' -> return parseObject(json, i)
            '[' -> return parseArrayAsValue(json, i)
            else -> return null
        }
    }

    private fun parseObject(json: String, start: Int): Pair<String, Int>? {
        var i = start
        if (json[i] != '{') return null
        val begin = i
        var depth = 1
        i++
        while (i < json.length && depth > 0) {
            when (json[i]) {
                '{' -> depth++
                '}' -> { depth--; if (depth == 0) return json.substring(begin, i + 1) to i + 1 }
                '"' -> i = skipString(json, i)
            }
            i++
        }
        return null
    }

    private fun parseArrayAsValue(json: String, start: Int): Pair<String, Int>? {
        var i = start
        if (json[i] != '[') return null
        val begin = i
        var depth = 1
        i++
        while (i < json.length && depth > 0) {
            when (json[i]) {
                '[' -> depth++
                ']' -> { depth--; if (depth == 0) return json.substring(begin, i + 1) to i + 1 }
                '"' -> i = skipString(json, i)
            }
            i++
        }
        return null
    }

    private fun skipString(json: String, start: Int): Int {
        var i = start
        if (json[i] != '"') return i
        i++
        while (i < json.length) {
            if (json[i] == '\\') i += 2
            else if (json[i] == '"') return i
            else i++
        }
        return i
    }

    private fun skipWhitespace(json: String, start: Int): Int {
        var i = start
        while (i < json.length && json[i].isWhitespace()) i++
        return i
    }

    private fun parseStroke(obj: String): Stroke? {
        val color = getLong(obj, KEY_COLOR) ?: return null
        val width = getFloat(obj, KEY_WIDTH) ?: return null
        val pointsStr = getArray(obj, KEY_POINTS) ?: return null
        val points = parsePointsArray(pointsStr)
        if (points.isEmpty()) return null
        return Stroke(color = color, width = width, points = points)
    }

    private fun parsePointsArray(arr: String): List<StrokePoint> {
        val list = parseArray(arr, 0) ?: return emptyList()
        return list.mapNotNull { parsePoint(it) }
    }

    private fun parsePoint(obj: String): StrokePoint? {
        val x = getFloat(obj, KEY_X) ?: return null
        val y = getFloat(obj, KEY_Y) ?: return null
        val p = getFloat(obj, KEY_P) ?: 1f
        val t = getLong(obj, KEY_T) ?: 0L
        return StrokePoint(x = x, y = y, pressure = p, timestamp = t)
    }

    private fun getLong(obj: String, key: String): Long? {
        val v = getNumber(obj, key) ?: return null
        return v.toLongOrNull()
    }

    private fun getFloat(obj: String, key: String): Float? {
        val v = getNumber(obj, key) ?: return null
        return v.toFloatOrNull()
    }

    private fun getNumber(obj: String, key: String): String? {
        val keyPattern = "\"$key\""
        val idx = obj.indexOf(keyPattern)
        if (idx < 0) return null
        var i = idx + keyPattern.length
        while (i < obj.length && obj[i].isWhitespace()) i++
        if (i >= obj.length || obj[i] != ':') return null
        i++
        while (i < obj.length && obj[i].isWhitespace()) i++
        if (i >= obj.length) return null
        val start = i
        if (obj[i] == '-') i++
        while (i < obj.length && (obj[i].isDigit() || obj[i] == '.')) i++
        return obj.substring(start, i)
    }

    private fun getArray(obj: String, key: String): String? {
        val keyPattern = "\"$key\""
        val idx = obj.indexOf(keyPattern)
        if (idx < 0) return null
        var i = idx + keyPattern.length
        while (i < obj.length && obj[i].isWhitespace()) i++
        if (i >= obj.length || obj[i] != ':') return null
        i++
        while (i < obj.length && obj[i].isWhitespace()) i++
        if (i >= obj.length || obj[i] != '[') return null
        val begin = i
        var depth = 1
        i++
        while (i < obj.length && depth > 0) {
            when (obj[i]) {
                '[' -> depth++
                ']' -> { depth--; if (depth == 0) return obj.substring(begin, i + 1) }
                '"' -> i = skipString(obj, i)
            }
            i++
        }
        return null
    }
}
