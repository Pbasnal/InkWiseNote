package com.originb.inkwisenote.commonutils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    public static String msToDateTime(long createdTimeMillis) {
        Instant instant = Instant.ofEpochMilli(createdTimeMillis);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        return formatter.format(instant);
    }
}
