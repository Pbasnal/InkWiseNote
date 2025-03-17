package com.originb.inkwisenote2.common

import java.util.*

object Strings {
    fun isNotEmpty(string: String?): Boolean {
        return !isNullOrWhitespace(string)
    }

    fun isNullOrWhitespace(string: String?): Boolean {
        if (Objects.isNull(string)) return true
        if (string!!.trim { it <= ' ' } === "") return true

        return false
    }

    fun isNumber(string: String): Boolean {
        try {
            string.toInt()
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    fun isLong(string: String): Boolean {
        try {
            string.toLong()
            return true
        } catch (ex: Exception) {
            return false
        }
    }
}
