package com.originb.inkwisenote2.common;

import android.content.Context;

public class ScreenUtils {
    public static int pxToDp(int px, Context context) {
        return Math.round(px * context.getResources().getDisplayMetrics().density);
    }
}
