package com.originb.inkwisenote2.common

import lombok.Getter

@Getter
enum class BitmapScale(private val value: Float) {
    // Common results
    FULL_SIZE(1f),
    THUMBNAIL(0.2f)
}
