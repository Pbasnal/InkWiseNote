package org.basnalcorp.shared.util

object MapsUtils {
    fun isEmpty(map: Map<*, *>?): Boolean =
        map == null || map.isEmpty()

    fun notEmpty(map: Map<*, *>): Boolean =
        !isEmpty(map)

    fun <K, V> mergeMapsWithSets(
        map1: Map<K, Set<V>>?,
        map2: Map<K, Set<V>>?
    ): MutableMap<K, MutableSet<V>> {
        val result = mutableMapOf<K, MutableSet<V>>()
        map1?.forEach { (key, set) -> result[key] = set.toMutableSet() }
        map2?.forEach { (key, set) ->
            result.getOrPut(key) { mutableSetOf() }.addAll(set)
        }
        return result
    }
}
