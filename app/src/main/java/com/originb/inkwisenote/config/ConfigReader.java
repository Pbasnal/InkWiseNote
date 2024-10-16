package com.originb.inkwisenote.config;

import android.content.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.originb.inkwisenote.R;
import lombok.Getter;

import java.io.InputStream;
import java.io.InputStreamReader;

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

    private ConfigReader(Context context) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.config);
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            appConfig = om.readValue(reader, AppConfig.class);
            reader.close();
            is.close();

            is = context.getResources().openRawResource(R.raw.app);
            reader = new InputStreamReader(is, "UTF-8");
            appConfig.setAppSecrets(om.readValue(reader, AppSecrets.class));
            reader.close();
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
}
