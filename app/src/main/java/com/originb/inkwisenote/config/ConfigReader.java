package com.originb.inkwisenote.config;

import android.content.Context;
import com.originb.inkwisenote.R;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static ConfigReader instance;

    private Properties properties;

    public static ConfigReader fromContext(Context context) {
        if (instance == null) {
            instance = new ConfigReader(context);
        }
        return instance;
    }

    private ConfigReader(Context context) {
        properties = new Properties();
        try {
            InputStream rawResource = context.getResources().openRawResource(R.raw.config);
            properties.load(rawResource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public boolean isFeatureEnabled(Feature featureName) {
        String featureFlag = "feature." + featureName;
        return Boolean.parseBoolean(properties.getProperty(featureFlag));
    }
}