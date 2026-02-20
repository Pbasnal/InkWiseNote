package com.originb.inkwisenote2.config

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.originb.inkwisenote2.R
import com.originb.inkwisenote2.common.Logger
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
            var `is` = context.getResources().openRawResource(R.raw.config)
            val reader = InputStreamReader(`is`, "UTF-8")
            appConfig = om.readValue<AppConfig>(reader, AppConfig::class.java)
            reader.close()
            `is`.close()

            if (isFeatureEnabled(Feature.AZURE_OCR)) {
                var appSecrets: AppSecrets = AppSecrets.Companion.loadFromEnv()
                if (!appSecrets.isAzureOcrEnabled()) {
                    `is` = context.getResources().openRawResource(R.raw.app)
                    appSecrets = AppSecrets.Companion.loadFromInputStream(`is`)
                }
                appConfig!!.setAppSecrets(appSecrets)
            }
            `is`.close()
        } catch (e: Exception) {
            appConfig = AppConfig.Companion.createDefault()
            e.printStackTrace()
        }
    }

    fun isFeatureEnabled(featureName: Feature?): Boolean {
        if (appConfig!!.getEnabledFeatures() != null) {
            return appConfig!!.getEnabledFeatures().contains(featureName)
        }
        return false
    }

    fun <T> runIfFeatureEnabled(feature: Feature, callable: Callable<T?>?): Optional<T?> {
        if (isFeatureEnabled(feature)) {
            Try.to<T?>(callable, Logger(feature.getFeatureName())).get()
        }
        return Optional.empty<T?>()
    }

    fun runIfFeatureEnabled(feature: Feature, runnable: Runnable?) {
        if (isFeatureEnabled(feature)) {
            Try.to<Any?>(runnable, Logger(feature.getFeatureName())).get()
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

        fun setRuntimeSetting(configKey: ConfigKeys?, value: String?) {
            getInstance().getAppConfig().getRuntimeSettings().put(configKey, value)
        }

        fun getRuntimeSetting(configKey: ConfigKeys?, defaultValue: String?): String? {
            return getInstance().getAppConfig().getRuntimeSettings().getOrDefault(configKey, defaultValue)
        }

        @JvmStatic
        val isAzureOcrEnabled: Boolean
            get() {
                val appSecrets: AppSecrets = getInstance().getAppConfig().getAppSecrets()
                return getInstance()
                    .isFeatureEnabled(Feature.AZURE_OCR) &&
                        appSecrets.isAzureOcrEnabled()
            }
    }
}
