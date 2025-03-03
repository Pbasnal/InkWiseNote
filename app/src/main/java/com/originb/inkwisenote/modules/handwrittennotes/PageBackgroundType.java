package com.originb.inkwisenote.modules.handwrittennotes;

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
