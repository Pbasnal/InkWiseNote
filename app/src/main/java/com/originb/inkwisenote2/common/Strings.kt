package com.originb.inkwisenote2.common

fun isNotEmpty(string: String?): Boolean {
    return !isNullOrWhitespace(string)
}


fun isNullOrWhitespace(string: String?): Boolean {
    if (string == null) return true
    if (string.trim().isEmpty()) return true

    return false
}


fun isNumber(string: String): Boolean {
    try {
        string.toInt()
        return true
    } catch (ex: Exception) {
        return false
    }
}


fun isLong(string: String): Boolean {
    try {
        string.toLong()
        return true
    } catch (ex: Exception) {
        return false
    }
}


fun focusedOnWord(allWordsStr: String?, wordToFocusOn: String?): String? {
    if (allWordsStr == null) return ""

    val allWords = allWordsStr.split("\\s+".toRegex()).filter { it.isNotEmpty() }

    // returning a joined string because there might be newline characters
    // and that will mess up the text on screen.
    // example, if string is "another\ntodo"
    // then on screen we will only see "another"
    if (allWords.size < 3) return allWords.joinToString(" ")

    val allWordsLower = allWords.map { it.lowercase() }
    val wordToFocusLower = wordToFocusOn?.lowercase()

    val indexOfFocusedWord = allWordsLower.indexOf(wordToFocusLower)
    if (indexOfFocusedWord == -1) {
        return null
    }

    val startOfString: String
    if (indexOfFocusedWord < 2) {
        startOfString = allWordsLower.subList(0, indexOfFocusedWord).joinToString(" ")
    } else {
        val start = indexOfFocusedWord - 1
        startOfString = "..." + allWordsLower.subList(start, indexOfFocusedWord).joinToString(" ")
    }

    val endOfString: String
    val remainingWords = allWordsLower.size - indexOfFocusedWord
    if (remainingWords < 2) {
        endOfString = allWordsLower.subList(indexOfFocusedWord, allWordsLower.size).joinToString(" ")
    } else {
        val end = indexOfFocusedWord + 2
        endOfString = allWordsLower.subList(indexOfFocusedWord, end).joinToString(" ") + "..."
    }

    return "$startOfString $endOfString"
}

