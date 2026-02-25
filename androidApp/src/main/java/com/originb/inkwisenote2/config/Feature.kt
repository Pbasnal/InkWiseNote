package com.originb.inkwisenote2.config

enum class Feature(featureName: String) {
    HOME_PAGE_NAVIGATION_SIDEBAR("HOME_PAGE_NAVIGATION_SIDEBAR"),
    HOME_PAGE_SIDEBAR_FOLDERS_LIST("HOME_PAGE_SIDEBAR_FOLDERS_LIST"),
    AZURE_OCR("AZURE_OCR"),
    MARKDOWN_EDITOR("MARKDOWN_EDITOR"),
    ADMIN_VIEW("ADMIN_VIEW"),
    CAMERA_NOTE("CAMERA_NOTE"),
    DRAWING("drawing");

    val featureName: String?

    init {
        this.featureName = featureName
    }
}