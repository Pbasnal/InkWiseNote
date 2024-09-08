package com.originb.inkwisenote.config;

import com.originb.inkwisenote.data.config.PageTemplate;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AppConfig {
    private List<Feature> enabledFeatures;
    private Map<String, PageTemplate> pageTemplates;


    public static AppConfig createDefault() {
        AppConfig appConfig = new AppConfig();
        appConfig.enabledFeatures = new ArrayList<>();
        appConfig.pageTemplates = new HashMap<>();

        return appConfig;
    }

}
