package com.originb.inkwisenote.modules.commonutils;

import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.modules.functionalUtils.Try;

import java.util.Objects;

public class Strings {

    public static boolean isNotEmpty(String string) {
        return !isNullOrWhitespace(string);
    }

    public static boolean isNullOrWhitespace(String string) {
        if (Objects.isNull(string)) return true;
        if (string.trim() == "") return true;

        return false;
    }

    public static boolean isNumber(String string) {
        return Try.to(() -> {
                    Integer.parseInt(string);
                    return true;
                }, new DebugContext("Checking If String is Number"))
                .get().orElse(false);
    }
}
