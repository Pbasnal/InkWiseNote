package com.originb.inkwisenote.views;

public enum PageTemplateType {
    BASIC_RULED_PAGE_TEMPLATE("BASIC_RULED_PAGE_TEMPLATE");

    private final String pageTemplateName;

    PageTemplateType(String pageTemplateName) {
        this.pageTemplateName = pageTemplateName;
    }

    public String getPageTemplateName() {
        return pageTemplateName;
    }
}
