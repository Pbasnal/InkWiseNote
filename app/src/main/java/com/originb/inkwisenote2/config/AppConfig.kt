package com.originb.inkwisenote2.config

import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate
import lombok.Getter
import lombok.Setter

@Getter
@Setter
class AppConfig {
    private val enabledFeatures: List<Feature> = ArrayList()
    private val pageTemplates: Map<String, PageTemplate> = HashMap()
    private val appSecrets = AppSecrets()

    private val runtimeSettings: Map<ConfigKeys, String> = HashMap()

    companion object {
        fun createDefault(): AppConfig {
            return AppConfig()
        }
    }
}

