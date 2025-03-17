package com.originb.inkwisenote2.common

import java.util.*
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.stream.Collectors

object ListUtils {
    fun <T> listOf(vararg items: T): List<T> {
        if (items == null) return ArrayList()
        return when (items.size) {
            0 -> emptyList()
            1 -> kotlin.collections.listOf(items[0])
            else -> Collections.unmodifiableList(Arrays.asList(*items))
        }
    }

    fun <T> merge(a: MutableList<T>?, b: List<T>?): List<T>? {
        if (a == null && b != null) return b
        else if (a != null && b == null) return a
        else if (a == null && b == null) return ArrayList()

        a!!.addAll(b!!)
        return a
    }

    fun <T, K> groupBy(list: List<T>?, keyMapper: Function<in T, out K>?): Map<K, MutableList<T>> {
        return list!!.stream().collect(
            Collectors.toMap(
                keyMapper,
                Function { obj: T -> listOf() },
                BinaryOperator { obj: ListUtils?, a: MutableList<T>?, b: List<T>? -> merge(a, b) })
        )
    }
}
