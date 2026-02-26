package org.basnalcorp.shared.util

object ListUtils {
    fun <T> listOf(vararg items: T?): List<T?> =
        items.toList()

    fun <T> merge(a: List<T?>?, b: List<T?>?): List<T?> {
        val result = mutableListOf<T?>()
        a?.let { result.addAll(it) }
        b?.let { result.addAll(it) }
        return result
    }

    fun <T, K> groupBy(list: List<T>, keyMapper: (T) -> K): Map<K, List<T>> =
        list.groupBy(keyMapper)
}
