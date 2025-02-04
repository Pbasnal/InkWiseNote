package com.originb.inkwisenote.ux.views;

public enum PageBackgroundType {
    BASIC_RULED_PAGE_TEMPLATE("BASIC_RULED_PAGE_TEMPLATE");

    private final String pageTemplateName;

    PageBackgroundType(String pageTemplateName) {
        this.pageTemplateName = pageTemplateName;
    }

    public String getPageTemplateName() {
        return pageTemplateName;
    }
}
