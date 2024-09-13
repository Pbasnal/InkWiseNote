package com.originb.inkwisenote.io;

import com.google.android.gms.common.util.Strings;
import com.originb.inkwisenote.constants.Returns;
import com.originb.inkwisenote.data.config.PageTemplate;
import com.originb.inkwisenote.filemanager.JsonFileManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PageTemplateRepository {
    private final File directory;
    private Map<Long, PageTemplateInfo> pageTemplates;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PageTemplateInfo {
        private String pageTemplatePath;
        private PageTemplate pageTemplate;
    }

    public PageTemplateRepository(File directory) {
        this.directory = directory;
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void loadAll() {
        File[] pageTemplates = directory.listFiles((dir, name) -> name.endsWith(".pt"));
        if (Objects.isNull(pageTemplates)) return;

        this.pageTemplates = new HashMap<>();
        for (int i = 0; i < pageTemplates.length; i++) {
            File pageTemplate = pageTemplates[i];
            String nameWithExtension = pageTemplate.getName();
            nameWithExtension = nameWithExtension.substring(0, nameWithExtension.lastIndexOf('.'));
            Long templateId = parseTemplateIdFromFileName(nameWithExtension);
            if (Objects.isNull(templateId)) continue;

            JsonFileManager.readDataFromDisk(pageTemplates[i].getPath(), PageTemplate.class)
                    .ifPresent(template -> {
                        template.setTemplateId(templateId);
                        this.pageTemplates.put(templateId, new PageTemplateInfo(pageTemplate.getPath(), template));
                    });
        }
    }

    public Optional<PageTemplate> getPageTemplate(Long templateId) {
        return Optional.ofNullable(pageTemplates.get(templateId))
                .map(PageTemplateInfo::getPageTemplate);
    }

    public Returns savePageTemplate(Long templateId, String path, String name, PageTemplate pageTemplate) {
        if (Objects.isNull(pageTemplate) || Strings.isEmptyOrWhitespace(path)) {
            return Returns.INVALID_ARGUMENTS;
        }

        String fullPath = path + "/" + name + ".pt";
        JsonFileManager.writeDataToDisk(fullPath, pageTemplate);

        if (pageTemplates.containsKey(templateId)) {
            PageTemplateInfo pageTemplateInfo = pageTemplates.get(templateId);
            pageTemplateInfo.setPageTemplate(pageTemplate);
        } else {
            pageTemplates.put(templateId, new PageTemplateInfo(fullPath, pageTemplate));
        }

        return Returns.SUCCESS;
    }

    private static Long parseTemplateIdFromFileName(String noteNameWithoutExtension) {
        if (noteNameWithoutExtension.contains("-")) {
            return Long.parseLong(noteNameWithoutExtension.split("-")[1]);
        } else if (noteNameWithoutExtension.contains("_")) {
            return Long.parseLong(noteNameWithoutExtension.split("_")[1]);
        }
        return null;
    }


}