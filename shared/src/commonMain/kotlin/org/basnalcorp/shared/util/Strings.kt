package org.basnalcorp.shared.util

fun isNullOrWhitespace(string: String?): Boolean {
    if (string == null) return true
    if (string.trim().isEmpty()) return true
    return false
}

fun isNotEmpty(string: String?): Boolean =
    !isNullOrWhitespace(string)

fun isNumber(string: String): Boolean =
    string.toIntOrNull() != null

fun isLong(string: String): Boolean =
    string.toLongOrNull() != null

/**
 * Returns a short excerpt around the focused word (e.g. for search result snippet).
 * Uses lowercase comparison; returns null if word not found.
 */
fun focusedOnWord(allWordsStr: String?, wordToFocusOn: String?): String? {
    if (allWordsStr == null) return ""
    val allWords = allWordsStr.split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (allWords.size < 3) return allWords.joinToString(" ")
    val allWordsLower = allWords.map { it.lowercase() }
    val wordToFocusLower = wordToFocusOn?.lowercase() ?: return null
    val indexOfFocusedWord = allWordsLower.indexOf(wordToFocusLower)
    if (indexOfFocusedWord == -1) return null
    val startOfString = if (indexOfFocusedWord < 2) {
        allWordsLower.subList(0, indexOfFocusedWord).joinToString(" ")
    } else {
        val start = indexOfFocusedWord - 1
        "..." + allWordsLower.subList(start, indexOfFocusedWord).joinToString(" ")
    }
    val remainingWords = allWordsLower.size - indexOfFocusedWord
    val endOfString = if (remainingWords < 2) {
        allWordsLower.subList(indexOfFocusedWord, allWordsLower.size).joinToString(" ")
    } else {
        val end = (indexOfFocusedWord + 2).coerceAtMost(allWordsLower.size)
        allWordsLower.subList(indexOfFocusedWord, end).joinToString(" ") + "..."
    }
    return "$startOfString $endOfString"
}
