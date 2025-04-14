package com.originb.inkwisenote2.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isLong(String string) {
        try {
            Long.parseLong(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String focusedOnWord(String allWordsStr, String wordToFocusOn) {
        if (allWordsStr == null) return "";

        String[] allWords = allWordsStr.split("\\s+");

        // returning a joined string because there might be newline characters
        // and that will mess up the text on screen.
        // example, if string is "another\ntodo"
        // then on screen we will only see "another"
        if (allWords.length < 3) return String.join(" ", allWords);

        List<String> allWordsList = Arrays.asList(allWords).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        int indexOfFocusedWord = allWordsList.indexOf(wordToFocusOn);
        if (indexOfFocusedWord == -1) {
            return null;
        }

        String startOfString;
        if (indexOfFocusedWord < 2) {
            startOfString = String.join(" ", allWordsList.subList(0, indexOfFocusedWord));
        } else {
            int start = indexOfFocusedWord - 1;
            startOfString = "..." + String.join(" ", allWordsList.subList(start, indexOfFocusedWord));
        }

        String endOfString;
        int remainingWords = allWordsList.size() - indexOfFocusedWord;
        if (remainingWords < 2) {
            endOfString = String.join(" ", allWordsList.subList(indexOfFocusedWord, allWordsList.size()));
        } else {
            int end = indexOfFocusedWord + 2;
            endOfString = String.join(" ", allWordsList.subList(indexOfFocusedWord, end)) + "...";
        }

        return startOfString + " " + endOfString;
    }
}
