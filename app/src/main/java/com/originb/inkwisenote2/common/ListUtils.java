package com.originb.inkwisenote2.common;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListUtils {
    public static <T> List<T> listOf(T... items) {
        if (items == null) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(items));
    }

    public static <T> List<T> merge(List<T> a, List<T> b) {
        if (a == null && b == null) return new ArrayList<>();
        else if (a != null && b == null) return new ArrayList<>(a);
        else if (a == null && b != null) return new ArrayList<>(b);

        List<T> result = new ArrayList<>(a);
        result.addAll(b);
        return result;
    }

    public static <T, K> Map<K, List<T>> groupBy(List<T> list, Function<? super T, ? extends K> keyMapper) {
        return list.stream().collect(Collectors.<T, K, List<T>>toMap(keyMapper, ListUtils::listOf, ListUtils::merge));
    }
}
