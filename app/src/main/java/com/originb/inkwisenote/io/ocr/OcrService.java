package com.originb.inkwisenote.io.ocr;

import android.os.AsyncTask;
import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.originb.inkwisenote.config.AppSecrets;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class OcrService {
    public static void convertHandwritingToText(InputStream imageStream, AppSecrets appSecrets, OcrCallback callback) {
        new AnalyzeImageTask(callback, appSecrets).execute(imageStream);
    }

    public static class AnalyzeImageTask extends AsyncTask<InputStream, Void, AzureOcrResult> {
        public String VISION_KEY = "";
        public String VISION_ENDPOINT = "";
        private OcrCallback callback;

        ObjectMapper om = new ObjectMapper();

        public AnalyzeImageTask(OcrCallback callback) {
            this.callback = callback;
        }
        public AnalyzeImageTask(OcrCallback callback, AppSecrets appSecrets) {
            this.callback = callback;
            VISION_KEY = appSecrets.visionApi.visionApiKey;
            VISION_ENDPOINT = appSecrets.visionApi.visionApiEndpoint;
        }

        @Override
        protected AzureOcrResult doInBackground(InputStream... params) {
            return runOcr(params[0]);
        }

        @Override
        protected void onPostExecute(AzureOcrResult result) {
            if (callback != null) {
                callback.onResult(result);
            }
        }

        public AzureOcrResult runOcr(InputStream imageStream) {
            try {
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
            } catch (IOException e) {
                Log.e("OcrService", "Error running OCR", e);
                return null;
            }
        }
    }

    public interface OcrCallback {
        void onResult(AzureOcrResult result);
    }
}
