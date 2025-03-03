package com.originb.inkwisenote.common;

import java.util.Map;
import java.util.Objects;

public class Maps {
    public static boolean isEmpty(Map<?, ?> map) {
        return (Objects.isNull(map) || map.isEmpty());
    }
}

