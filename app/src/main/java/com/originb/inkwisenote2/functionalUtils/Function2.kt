package com.originb.inkwisenote2.functionalUtils

import com.originb.inkwisenote2.R

fun interface Function2<A, B, R> {
    fun apply(a: A, b: B): R
}
