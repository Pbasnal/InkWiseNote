package com.originb.inkwisenote2.common

object MapsUtils {
    @JvmStatic
    fun isEmpty(map: MutableMap<*, *>?): Boolean {
        return (map == null || map.isEmpty())
    }

    @JvmStatic
    fun notEmpty(map: MutableMap<*, *>): Boolean {
        return !isEmpty(map)
    }

    /**
     * Merges two maps where values are lists. If both maps contain the same key,
     * the lists are combined. If a key exists in only one map, it's added to the result.
     *
     * @param map1 First map to merge
     * @param map2 Second map to merge
     * @param <K> Type of the key
     * @param <V> Type of the list elements
     * @return A new map containing all keys and merged lists
    </V></K> */
    @JvmStatic
    fun <K, V> mergeMapsWithSets(
        map1: Map<K, Set<V>>?,
        map2: Map<K, Set<V>>?
    ): MutableMap<K, MutableSet<V>> {
        val result = mutableMapOf<K, MutableSet<V>>()

        // Add all entries from first map
        map1?.forEach { (key, set) ->
            result[key] = set.toMutableSet()
        }

        // Merge entries from second map
        map2?.forEach { (key, set) ->
            result[key]?.addAll(set) ?: run { result[key] = set.toMutableSet() }
        }

        return result
    }
}

