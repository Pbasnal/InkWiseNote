package com.originb.inkwisenote.config;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.originb.inkwisenote.R;
import lombok.Getter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

@Getter
public class ConfigReader {
    private AppConfig appConfig;

    @Getter
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
            Gson gson = new Gson();

            Type type = new TypeToken<AppConfig>() {
            }.getType();
            appConfig = gson.fromJson(reader, type);
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
