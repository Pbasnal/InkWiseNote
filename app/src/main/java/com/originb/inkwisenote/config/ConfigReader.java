package com.originb.inkwisenote.config;

import android.content.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.originb.inkwisenote.common.Logger;
import com.originb.inkwisenote.R;
import com.originb.inkwisenote.functionalUtils.Try;
import lombok.Getter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.Callable;

public class ConfigReader {
    @Getter
    private AppConfig appConfig;

    private ObjectMapper om = new ObjectMapper();

    private static ConfigReader instance;

    public static ConfigReader fromContext(Context context) {
        if (instance == null) {
            instance = new ConfigReader(context);
        }
        return instance;
    }

    public static ConfigReader getInstance() {
        if (instance == null) {
            throw new RuntimeException("Configs were not initialized");
        }
        return instance;
    }

    private ConfigReader(Context context) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.config);
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            appConfig = om.readValue(reader, AppConfig.class);
            reader.close();
            is.close();

            if (isFeatureEnabled(Feature.AZURE_OCR)) {
                AppSecrets appSecrets = AppSecrets.loadFromEnv();
                if (!appSecrets.isAzureOcrEnabled()) {
                    is = context.getResources().openRawResource(R.raw.app);
                    appSecrets = AppSecrets.loadFromInputStream(is);
                }
                appConfig.setAppSecrets(appSecrets);
            }
            is.close();
        } catch (Exception e) {
            appConfig = AppConfig.createDefault();
            e.printStackTrace();
        }
    }

    public boolean isFeatureEnabled(Feature featureName) {
        if (appConfig.getEnabledFeatures() != null) {
            return appConfig.getEnabledFeatures().contains(featureName);
        }
        return false;
    }

    public <T> Optional<T> runIfFeatureEnabled(Feature feature, Callable<T> callable) {
        if (isFeatureEnabled(feature)) {
            Try.to(callable, new Logger(feature.getFeatureName())).get();
        }
        return Optional.empty();
    }

    public void runIfFeatureEnabled(Feature feature, Runnable runnable) {
        if (isFeatureEnabled(feature)) {
            Try.to(runnable, new Logger(feature.getFeatureName())).get();
        }
    }

    public static void setRuntimeSetting(ConfigKeys configKey, String value) {
        getInstance().getAppConfig().getRuntimeSettings().put(configKey, value);
    }

    public static String getRuntimeSetting(ConfigKeys configKey, String defaultValue) {
        return getInstance().getAppConfig().getRuntimeSettings().getOrDefault(configKey, defaultValue);
    }

    public static boolean isAzureOcrEnabled() {
        AppSecrets appSecrets = getInstance().getAppConfig().getAppSecrets();
        return getInstance().isFeatureEnabled(Feature.AZURE_OCR) &&
                appSecrets.isAzureOcrEnabled();
    }
}
