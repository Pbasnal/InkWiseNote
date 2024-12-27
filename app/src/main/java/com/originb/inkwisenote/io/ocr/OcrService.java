package com.originb.inkwisenote.io.ocr;

import android.os.AsyncTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.Strings;
import com.originb.inkwisenote.DebugContext;
import com.originb.inkwisenote.config.AppSecrets;
import com.originb.inkwisenote.modules.functionalUtils.Try;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class OcrService {
    public static AnalyzeImageTask convertHandwritingToText(InputStream imageStream, AppSecrets appSecrets, OcrCallback callback) {
        AnalyzeImageTask analyzeImageTask = new AnalyzeImageTask(callback, appSecrets);
        analyzeImageTask.execute(imageStream);
        return analyzeImageTask;
    }

    public static AnalyzeImageTask convertHandwritingToText(InputStream imageStream, AppSecrets appSecrets) {
        AnalyzeImageTask analyzeImageTask = new AnalyzeImageTask(appSecrets);
        analyzeImageTask.execute(imageStream);
        return analyzeImageTask;
    }


    public static class AnalyzeImageTask extends AsyncTask<InputStream, Void, AzureOcrResult> {
        public String VISION_KEY = "";
        public String VISION_ENDPOINT = "";
        private OcrCallback callback;

        ObjectMapper om = new ObjectMapper();

        public AnalyzeImageTask(AppSecrets appSecrets) {
            this.callback = null;
            VISION_KEY = appSecrets.visionApi.visionApiKey;
            VISION_ENDPOINT = appSecrets.visionApi.visionApiEndpoint;
        }

        public AnalyzeImageTask(OcrCallback callback, AppSecrets appSecrets) {
            this.callback = callback;
            VISION_KEY = appSecrets.visionApi.visionApiKey;
            VISION_ENDPOINT = appSecrets.visionApi.visionApiEndpoint;
        }

        @Override
        protected AzureOcrResult doInBackground(InputStream... params) {
            if (Strings.isEmptyOrWhitespace(VISION_KEY) || Strings.isEmptyOrWhitespace(VISION_ENDPOINT)) {
                return null;
            }

            return Try.to(() -> runOcr(params[0])
                            , new DebugContext("Azure Ocr Service"))
                    .logIfError("Error running OCR")
                    .get()
                    .orElseGet(null);

        }

        public AzureOcrResult runOcr(InputStream imageStream) throws IOException {
            byte[] imageBytes = new byte[imageStream.available()];
            imageStream.read(imageBytes);
//                String contents = Base64.getEncoder().encodeToString(imageBytes);

            URL url = new URL(VISION_ENDPOINT + "/computervision/imageanalysis:analyze?api-version=2023-02-01-preview&features=read&language=en");
            HttpURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", VISION_KEY);
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(imageBytes, 0, imageBytes.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return om.readValue(response.toString(), AzureOcrResult.class);
                }
            } else {
                throw new RuntimeException("Error response code: " + responseCode);
            }
        }

        @Override
        protected void onPostExecute(AzureOcrResult result) {
            if (callback != null) {
                callback.onResult(result);
            }
        }
    }

    public interface OcrCallback {
        void onResult(AzureOcrResult result);
    }
}
