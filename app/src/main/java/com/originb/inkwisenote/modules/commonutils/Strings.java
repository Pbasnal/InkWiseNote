package com.originb.inkwisenote.modules.commonutils;

import java.util.Objects;

public class Strings {
    public static boolean isNullOrWhitespace(String string) {
        if (Objects.isNull(string)) return true;
        if (string.trim() == "") return true;

        return false;
    }
}
