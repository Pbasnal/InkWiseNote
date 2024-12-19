package com.originb.inkwisenote.config;

public enum Feature {
    HOME_PAGE_NAVIGATION_SIDEBAR("HOME_PAGE_NAVIGATION_SIDEBAR"),
    HOME_PAGE_SIDEBAR_FOLDERS_LIST("HOME_PAGE_SIDEBAR_FOLDERS_LIST"),
    MARKDOWN_EDITOR("MARKDOWN_EDITOR"),
    ADMIN_VIEW("ADMIN_VIEW"),
    CAMERA_NOTE("CAMERA_NOTE"),
    DRAWING("drawing");

    private final String featureName;

    Feature(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return featureName;
    }
}