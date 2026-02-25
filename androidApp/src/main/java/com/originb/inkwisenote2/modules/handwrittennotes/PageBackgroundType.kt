package com.originb.inkwisenote2.modules.handwrittennotes

enum class PageBackgroundType(pageTemplateName: String) {
    BASIC_RULED_PAGE_TEMPLATE("BASIC_RULED_PAGE_TEMPLATE");

    val pageTemplateName: String?

    init {
        this.pageTemplateName = pageTemplateName
    }
}
