package org.basnalcorp.shared.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringsTest {

    @Test
    fun isNullOrWhitespace_null_returnsTrue() {
        assertTrue(isNullOrWhitespace(null))
    }

    @Test
    fun isNullOrWhitespace_empty_returnsTrue() {
        assertTrue(isNullOrWhitespace(""))
        assertTrue(isNullOrWhitespace("   "))
    }

    @Test
    fun isNullOrWhitespace_nonEmpty_returnsFalse() {
        assertFalse(isNullOrWhitespace("a"))
        assertFalse(isNullOrWhitespace("  a  "))
    }

    @Test
    fun isNotEmpty_oppositeOfIsNullOrWhitespace() {
        assertFalse(isNotEmpty(null))
        assertFalse(isNotEmpty(""))
        assertTrue(isNotEmpty("x"))
    }

    @Test
    fun isNumber_validIntegers_returnsTrue() {
        assertTrue(isNumber("0"))
        assertTrue(isNumber("42"))
        assertTrue(isNumber("-1"))
    }

    @Test
    fun isNumber_invalid_returnsFalse() {
        assertFalse(isNumber(""))
        assertFalse(isNumber("1.5"))
        assertFalse(isNumber("abc"))
    }

    @Test
    fun focusedOnWord_nullInput_returnsEmptyOrNull() {
        assertTrue(focusedOnWord(null, "word") == "")
        assertTrue(focusedOnWord("hello world", null) == null)
    }

    @Test
    fun focusedOnWord_wordFound_returnsExcerpt() {
        val result = focusedOnWord("one two three four five", "three")
        assertTrue(result != null)
        assertTrue(result!!.contains("three"))
    }

    @Test
    fun focusedOnWord_wordNotFound_returnsNull() {
        assertTrue(focusedOnWord("one two three", "missing") == null)
    }
}
