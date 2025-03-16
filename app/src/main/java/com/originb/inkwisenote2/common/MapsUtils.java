package com.originb.inkwisenote2.common;

import java.util.Map;
import java.util.Objects;

public class MapsUtils {
    public static boolean isEmpty(Map<?, ?> map) {
        return (Objects.isNull(map) || map.isEmpty());
    }
}

