package com.originb.inkwisenote.common;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListUtils {
    public static <T> List<T> listOf(@NonNull T... items) {
        switch (items.length) {
            case 0:
                return Collections.emptyList();
            case 1:
                return Collections.singletonList(items[0]);
            default:
                return Collections.unmodifiableList(Arrays.asList(items));
        }
    }
}
