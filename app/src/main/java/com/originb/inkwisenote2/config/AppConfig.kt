package com.originb.inkwisenote2.config

import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate

class AppConfig {
    var enabledFeatures: MutableList<Feature?>? = ArrayList()
    private val pageTemplates: MutableMap<String?, PageTemplate?>? = HashMap()
    private val canvasSizes: MutableList<CanvasSize?>? = ArrayList()
    var appSecrets: AppSecrets? = AppSecrets()
    private val runtimeSettings: MutableMap<ConfigKeys?, String?>? = HashMap()

    fun getRuntimeSettings(): MutableMap<ConfigKeys?, String?>? = runtimeSettings
    fun getPageTemplates(): MutableMap<String?, PageTemplate?>? = pageTemplates
    fun getCanvasSizes(): MutableList<CanvasSize?>? = canvasSizes

    companion object {
        fun createDefault(): AppConfig {
            return AppConfig()
        }
    }
}

