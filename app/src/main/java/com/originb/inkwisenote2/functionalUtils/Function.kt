package com.originb.inkwisenote2.functionalUtils

import com.originb.inkwisenote2.R

fun interface Function<A, B, C, R> {
    fun apply(a: A, b: B, c: C): R
}

