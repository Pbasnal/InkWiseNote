package com.originb.inkwisenote2.functionalUtils

fun interface Function2<A, B, R> {
    fun apply(a: A?, b: B?): R?
}
