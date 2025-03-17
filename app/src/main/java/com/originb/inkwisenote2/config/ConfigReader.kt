package com.originb.inkwisenote2.config

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.*
import com.originb.inkwisenote2.functionalUtils.Try
import lombok.Getter
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.Callable

class ConfigReader private constructor(context: Context) {
    @Getter
    private var appConfig: AppConfig? = null

    private val om = ObjectMapper()

    init {
        try {
            var `is` = context.resources.openRawResource(R.raw.config)
            val reader = InputStreamReader(`is`, "UTF-8")
            appConfig = om.readValue(reader, AppConfig::class.java)
            reader.close()
            `is`.close()

            if (isFeatureEnabled(Feature.AZURE_OCR)) {
                var appSecrets: AppSecrets = AppSecrets.Companion.loadFromEnv()
                if (!appSecrets.isAzureOcrEnabled) {
                    `is` = context.resources.openRawResource(R.raw.app)
                    appSecrets = AppSecrets.Companion.loadFromInputStream(`is`)
                }
                appConfig.appSecrets = appSecrets
            }
            `is`.close()
        } catch (e: Exception) {
            appConfig = AppConfig.Companion.createDefault()
            e.printStackTrace()
        }
    }

    fun isFeatureEnabled(featureName: Feature?): Boolean {
        if (appConfig.getEnabledFeatures() != null) {
            return appConfig.getEnabledFeatures().contains(featureName)
        }
        return false
    }

    fun <T> runIfFeatureEnabled(feature: Feature, callable: Callable<T>): Optional<T> {
        if (isFeatureEnabled(feature)) {
            Try.Companion.to<T>(callable, Logger(feature.featureName)).get()
        }
        return Optional.empty()
    }

    fun runIfFeatureEnabled(feature: Feature, runnable: Runnable) {
        if (isFeatureEnabled(feature)) {
            Try.Companion.to<Any>(runnable, Logger(feature.featureName)).get()
        }
    }

    companion object {
        private var instance: ConfigReader? = null

        fun fromContext(context: Context): ConfigReader? {
            if (instance == null) {
                instance = ConfigReader(context)
            }
            return instance
        }

        fun getInstance(): ConfigReader? {
            if (instance == null) {
                throw RuntimeException("Configs were not initialized")
            }
            return instance
        }

        fun setRuntimeSetting(configKey: ConfigKeys?, value: String?) {
            getInstance().getAppConfig().getRuntimeSettings().put(configKey, value)
        }

        fun getRuntimeSetting(configKey: ConfigKeys?, defaultValue: String?): String {
            return getInstance().getAppConfig().getRuntimeSettings().getOrDefault(configKey, defaultValue)
        }

        val isAzureOcrEnabled: Boolean
            get() {
                val appSecrets: AppSecrets = getInstance().getAppConfig().getAppSecrets()
                return getInstance()!!.isFeatureEnabled(Feature.AZURE_OCR) &&
                        appSecrets.isAzureOcrEnabled
            }
    }
}
