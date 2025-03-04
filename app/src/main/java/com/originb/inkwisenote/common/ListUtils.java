package com.originb.inkwisenote.common;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListUtils {
    public static <T> List<T> listOf(T... items) {
        if (items == null) return new ArrayList<>();
        switch (items.length) {
            case 0:
                return Collections.emptyList();
            case 1:
                return Collections.singletonList(items[0]);
            default:
                return Collections.unmodifiableList(Arrays.asList(items));
        }
    }

    public static <T> List<T> merge(List<T> a, List<T> b) {
        if (a == null && b != null) return b;
        else if (a != null && b == null) return a;
        else if (a == null && b == null) return new ArrayList<>();

        a.addAll(b);
        return a;
    }

    public static <T, K> Map<K, List<T>> groupBy(List<T> list, Function<? super T, ? extends K> keyMapper) {
        return list.stream().collect(Collectors.<T, K, List<T>>toMap(keyMapper, ListUtils::listOf, ListUtils::merge));
    }
}
