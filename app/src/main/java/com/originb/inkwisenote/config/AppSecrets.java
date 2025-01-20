package com.originb.inkwisenote.config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class AppSecrets {
    public VisionApi visionApi;

    @AllArgsConstructor
    @NoArgsConstructor
    public static class VisionApi {
        public String visionApiKey;
        public String visionApiEndpoint;
    }

    @Override
    public String toString() {
        return visionApi.visionApiKey + " | " + visionApi.visionApiEndpoint;
    }
}
