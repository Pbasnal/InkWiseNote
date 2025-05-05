package com.originb.inkwisenote2.config;

import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate;
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
    private List<CanvasSize> canvasSizes;
    private AppSecrets appSecrets;

    private Map<ConfigKeys, String> runtimeSettings;

    public AppConfig() {
        enabledFeatures = new ArrayList<>();
        pageTemplates = new HashMap<>();
        appSecrets = new AppSecrets();
        runtimeSettings = new HashMap<>();
        canvasSizes = new ArrayList<>();
    }

    public static AppConfig createDefault() {
        return new AppConfig();
    }

}

