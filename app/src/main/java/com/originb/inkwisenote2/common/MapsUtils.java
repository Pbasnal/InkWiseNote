package com.originb.inkwisenote2.common;

import java.util.*;
import java.util.Objects;

public class MapsUtils {
    public static boolean isEmpty(Map<?, ?> map) {
        return (Objects.isNull(map) || map.isEmpty());
    }

    public static boolean notEmpty(Map<?, ?> map) {
        return !isEmpty(map);
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
     */
    public static <K, V> Map<K, List<V>> mergeMapsWithLists(Map<K, List<V>> map1, Map<K, List<V>> map2) {
        if (isEmpty(map1) && isEmpty(map2)) {
            return new HashMap<>();
        }
        
        Map<K, List<V>> result = new HashMap<>();
        
        // Add all entries from first map
        if (notEmpty(map1)) {
            map1.forEach((key, list) -> result.put(key, new ArrayList<>(list)));
        }
        
        // Merge entries from second map
        if (notEmpty(map2)) {
            map2.forEach((key, list) -> {
                if (result.containsKey(key)) {
                    // Key exists, merge lists
                    result.get(key).addAll(list);
                } else {
                    // New key, add new list
                    result.put(key, new ArrayList<>(list));
                }
            });
        }
        
        return result;
    }
}

