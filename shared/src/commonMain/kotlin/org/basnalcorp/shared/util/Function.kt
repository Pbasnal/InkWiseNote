package org.basnalcorp.shared.util

fun interface Function<A, B, C, R> {
    fun apply(a: A?, b: B?, c: C?): R?
}
