package com.originb.inkwisenote.common;

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
        try{
            Integer.parseInt(string);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static boolean isLong(String string) {
        try{
            Long.parseLong(string);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }
}
