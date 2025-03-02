package com.originb.inkwisenote.constants;

import lombok.Getter;

@Getter
public enum BitmapScale {
    // Common results
    FULL_SIZE(1f),
    THUMBNAIL(0.2f);

    private final float value;

    BitmapScale(float value) {
        this.value = value;
    }

}
