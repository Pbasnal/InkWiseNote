package com.originb.inkwisenote.data;

import com.originb.inkwisenote.data.config.PageTemplate;
import com.originb.inkwisenote.filemanager.FileInfo;
import com.originb.inkwisenote.filemanager.FileType;

public class PageTemplateFileInfo extends FileInfo<PageTemplate> {
    public PageTemplateFileInfo(String filePath) {
        super(filePath, FileType.PAGE_TEMPLATE, PageTemplate.class);
    }

    public PageTemplateFileInfo(String filePath, PageTemplate data) {
        super(filePath, FileType.PAGE_TEMPLATE, data);
    }
}
