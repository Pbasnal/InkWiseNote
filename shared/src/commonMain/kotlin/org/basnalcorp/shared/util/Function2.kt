package org.basnalcorp.shared.util

fun interface Function2<A, B, R> {
    fun apply(a: A?, b: B?): R?
}
