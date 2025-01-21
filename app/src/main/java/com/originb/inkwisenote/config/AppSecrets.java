package com.originb.inkwisenote.config;

import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.originb.inkwisenote.BuildConfig;
import com.originb.inkwisenote.modules.commonutils.Strings;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.io.InputStreamReader;

@AllArgsConstructor
@NoArgsConstructor
public class AppSecrets {
    public VisionApi visionApi;

    private static ObjectMapper om = new ObjectMapper();

    @AllArgsConstructor
    @NoArgsConstructor
    public static class VisionApi {
        public String visionApiKey;
        public String visionApiEndpoint;
    }

    public static AppSecrets loadFromEnv() {
        VisionApi visionApi = new VisionApi(BuildConfig.VISION_API_KEY, BuildConfig.VISION_API_ENDPOINT);
        return new AppSecrets(visionApi);
    }

    public static AppSecrets loadFromInputStream(InputStream is) {
        try {
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            AppSecrets appSecrets = om.readValue(reader, AppSecrets.class);
            reader.close();
            return appSecrets;
        } catch (Exception ex) {
            Log.e("AppSecrets", "Failed to load from the input stream", ex);
        }

        return new AppSecrets(new VisionApi());
    }

    public boolean isAzureOcrEnabled() {
        return Strings.isNotEmpty(visionApi.visionApiEndpoint) &&
                Strings.isNotEmpty(visionApi.visionApiKey);
    }

    @Override
    public String toString() {
        return visionApi.visionApiKey + " | " + visionApi.visionApiEndpoint;
    }
}
