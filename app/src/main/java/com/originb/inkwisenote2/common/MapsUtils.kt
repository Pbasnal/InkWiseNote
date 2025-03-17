package com.originb.inkwisenote2.common

import java.util.*

object MapsUtils {
    fun isEmpty(map: Map<*, *>): Boolean {
        return (Objects.isNull(map) || map.isEmpty())
    }
}

