package com.originb.inkwisenote2.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    fun msToDateTime(createdTimeMillis: Long): String {
        val instant = Instant.ofEpochMilli(createdTimeMillis)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())

        return formatter.format(instant)
    }
}
