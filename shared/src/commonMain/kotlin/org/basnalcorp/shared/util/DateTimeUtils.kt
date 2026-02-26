package org.basnalcorp.shared.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object DateTimeUtils {
    /**
     * Formats epoch millis as "yyyy-MM-dd HH:mm:ss" in the default time zone.
     */
    fun msToDateTime(createdTimeMillis: Long): String {
        val instant = Instant.fromEpochMilliseconds(createdTimeMillis)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val y = local.year
        val m = local.monthNumber.toString().padStart(2, '0')
        val d = local.dayOfMonth.toString().padStart(2, '0')
        val h = local.hour.toString().padStart(2, '0')
        val min = local.minute.toString().padStart(2, '0')
        val s = local.second.toString().padStart(2, '0')
        return "$y-$m-$d $h:$min:$s"
    }
}
