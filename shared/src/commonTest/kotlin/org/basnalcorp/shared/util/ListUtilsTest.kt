package org.basnalcorp.shared.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ListUtilsTest {

    @Test
    fun merge_bothNull_returnsEmpty() {
        val result = ListUtils.merge<String>(null, null)
        assertEquals(emptyList<String?>(), result)
    }

    @Test
    fun merge_firstNull_returnsSecond() {
        val result = ListUtils.merge<String>(null, listOf("a", "b"))
        assertEquals(listOf("a", "b"), result)
    }

    @Test
    fun merge_secondNull_returnsFirst() {
        val result = ListUtils.merge(listOf("a", "b"), null)
        assertEquals(listOf("a", "b"), result)
    }

    @Test
    fun merge_bothNonEmpty_returnsConcatenation() {
        val result = ListUtils.merge(listOf(1, 2), listOf(3, 4))
        assertEquals(listOf(1, 2, 3, 4), result)
    }

    @Test
    fun groupBy_groupsCorrectly() {
        val list = listOf("aa", "ab", "ba", "bb")
        val result = ListUtils.groupBy(list) { it.first() }
        assertEquals(2, result.size)
        assertEquals(listOf("aa", "ab"), result['a'])
        assertEquals(listOf("ba", "bb"), result['b'])
    }
}
