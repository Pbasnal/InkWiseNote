package com.originb.inkwisenote2.config

import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate
import lombok.Getter
import lombok.Setter

@Getter
@Setter
class AppConfig {
    private val enabledFeatures: MutableList<Feature?>?
    private val pageTemplates: MutableMap<String?, PageTemplate?>?
    private val canvasSizes: MutableList<CanvasSize?>?
    private val appSecrets: AppSecrets?

    private val runtimeSettings: MutableMap<ConfigKeys?, String?>?

    init {
        enabledFeatures = ArrayList<Feature?>()
        pageTemplates = HashMap<String?, PageTemplate?>()
        appSecrets = AppSecrets()
        runtimeSettings = HashMap<ConfigKeys?, String?>()
        canvasSizes = ArrayList<CanvasSize?>()
    }

    companion object {
        fun createDefault(): AppConfig {
            return AppConfig()
        }
    }
}

