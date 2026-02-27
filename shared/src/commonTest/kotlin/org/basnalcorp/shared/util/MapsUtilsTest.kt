package org.basnalcorp.shared.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MapsUtilsTest {

    @Test
    fun isEmpty_nullOrEmpty_returnsTrue() {
        assertTrue(MapsUtils.isEmpty(null))
        assertTrue(MapsUtils.isEmpty(emptyMap<Any, Any>()))
    }

    @Test
    fun isEmpty_nonEmpty_returnsFalse() {
        assertFalse(MapsUtils.isEmpty(mapOf(1 to "a")))
    }

    @Test
    fun notEmpty_nonEmpty_returnsTrue() {
        assertTrue(MapsUtils.notEmpty(mapOf(1 to "a")))
    }

    @Test
    fun mergeMapsWithSets_mergesCorrectly() {
        val map1 = mapOf("a" to setOf(1L, 2L), "b" to setOf(3L))
        val map2 = mapOf("a" to setOf(4L), "c" to setOf(5L))
        val result = MapsUtils.mergeMapsWithSets(map1, map2)
        assertTrue(result["a"]!!.containsAll(setOf(1L, 2L, 4L)))
        assertEquals(setOf(3L), result["b"]!!.toSet())
        assertEquals(setOf(5L), result["c"]!!.toSet())
    }
}
