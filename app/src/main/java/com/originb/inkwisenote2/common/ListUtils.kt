package com.originb.inkwisenote2.common

import java.util.function.Function

object ListUtils {
    @JvmStatic
    fun <T> listOf(vararg items: T?): List<T?> {
        return items.toList()
    }

    @JvmStatic
    fun <T> merge(a: List<T?>?, b: List<T?>?): List<T?> {
        val result = mutableListOf<T?>()
        a?.let { result.addAll(it) }
        b?.let { result.addAll(it) }
        return result
    }

    @JvmStatic
    fun <T, K> groupBy(list: List<T>, keyMapper: Function<T, K>): Map<K, List<T>> {
        return list.groupBy(keyMapper::apply).mapValues { it.value }
    }
}
