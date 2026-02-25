package com.originb.inkwisenote2.config

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.functionalUtils.Try
import java.io.InputStreamReader

import java.util.concurrent.Callable

class ConfigReader private constructor(context: Context) {
    private var appConfig: AppConfig

    private val om = ObjectMapper()

    fun getAppConfig(): AppConfig = appConfig

    init {
        try {
            var inputStream = context.resources.openRawResource(R.raw.config)
            val reader = InputStreamReader(inputStream, "UTF-8")
            appConfig = om.readValue(reader, AppConfig::class.java)
            reader.close()
            inputStream.close()

            if (isFeatureEnabled(Feature.AZURE_OCR)) {
                var appSecrets: AppSecrets = AppSecrets.loadFromEnv()
                if (!appSecrets.isAzureOcrEnabled) {
                    inputStream = context.resources.openRawResource(R.raw.app)
                    AppSecrets.loadFromInputStream(inputStream).let { appSecrets = it }
                }
                appConfig.appSecrets = appSecrets
            }
            inputStream.close()
        } catch (e: Exception) {
            appConfig = AppConfig.createDefault()
            e.printStackTrace()
        }
    }

    fun isFeatureEnabled(featureName: Feature?): Boolean {
        val features = appConfig.enabledFeatures
        return features.contains(featureName)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> runIfFeatureEnabled(feature: Feature, callable: Callable<T?>): T? {
        if (isFeatureEnabled(feature)) {
            return Try.to(callable, Logger(feature.featureName)).get()
        }
        return null
    }

    fun runIfFeatureEnabled(feature: Feature, runnable: Runnable) {
        if (isFeatureEnabled(feature) ) {
            Try.to<Any?>(runnable, Logger(feature.featureName)).get()
        }
    }

    companion object {
        private var instance: ConfigReader? = null

        @JvmStatic
        fun fromContext(context: Context): ConfigReader {
            if (instance == null) {
                instance = ConfigReader(context)
            }
            return instance!!
        }

        @JvmStatic
        fun getInstance(): ConfigReader {
            if (instance == null) {
                throw RuntimeException("Configs were not initialized")
            }
            return instance!!
        }

        fun setRuntimeSetting(configKey: ConfigKeys, value: String) {
            getInstance().getAppConfig().getRuntimeSettings()[configKey] = value
        }

        fun getRuntimeSetting(configKey: ConfigKeys, defaultValue: String): String {
            return getInstance().getAppConfig().getRuntimeSettings().getOrDefault(configKey, defaultValue)
        }

        @JvmStatic
        val isAzureOcrEnabled: Boolean
            get() {
                val appSecrets = getInstance().getAppConfig().appSecrets!!
                return getInstance().isFeatureEnabled(Feature.AZURE_OCR) &&
                    appSecrets.isAzureOcrEnabled
            }
    }
}
