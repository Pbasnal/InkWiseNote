package com.originb.inkwisenote2.common

import android.content.Context
import kotlin.math.round

object ScreenUtils {
    @JvmStatic
    fun pxToDp(px: Int, context: Context): Int {
        return round(px * context.resources.displayMetrics.density).toInt()
    }
}
