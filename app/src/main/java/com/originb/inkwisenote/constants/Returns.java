package com.originb.inkwisenote.constants;

import lombok.Getter;

@Getter
public enum Returns {
    // Common results
    INVALID_ARGUMENTS("Invalid arguments"),
    SUCCESS("Success"),

    // Note Repository results
    NOTE_ALREADY_EXISTS("Note already exists"),
    NOTE_DOESNT_EXISTS("Note doesn't exists"),

    // Bitmap Repository results
    BITMAP_ALREADY_EXISTS("Bitmap already exists"),
    BITMAP_DOESNT_EXISTS("Bitmap doesn't exists");


    private final String result;

    Returns(String result) {
        this.result = result;
    }

}

