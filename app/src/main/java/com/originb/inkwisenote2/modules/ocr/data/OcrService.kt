package com.originb.inkwisenote2.modules.ocr.data

import android.os.AsyncTask
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.common.util.Strings
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.config.AppSecrets
import com.originb.inkwisenote2.functionalUtils.Try.Companion.to
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object OcrService {
    fun convertHandwritingToText(
        imageStream: InputStream?,
        appSecrets: AppSecrets,
        callback: OcrCallback?
    ): AnalyzeImageTask {
        val analyzeImageTask = AnalyzeImageTask(callback, appSecrets)
        analyzeImageTask.execute(imageStream)
        return analyzeImageTask
    }

    fun convertHandwritingToText(imageStream: InputStream?, appSecrets: AppSecrets): AnalyzeImageTask {
        val analyzeImageTask = AnalyzeImageTask(appSecrets)
        analyzeImageTask.execute(imageStream)
        return analyzeImageTask
    }


    class AnalyzeImageTask : AsyncTask<InputStream?, Void?, AzureOcrResult?> {
        var VISION_KEY: String? = ""
        var VISION_ENDPOINT: String? = ""
        private val callback: OcrCallback?

        var om: ObjectMapper = ObjectMapper()

        constructor(appSecrets: AppSecrets) {
            this.callback = null
            VISION_KEY = appSecrets.visionApi!!.visionApiKey
            VISION_ENDPOINT = appSecrets.visionApi!!.visionApiEndpoint
        }

        constructor(callback: OcrCallback?, appSecrets: AppSecrets) {
            this.callback = callback
            VISION_KEY = appSecrets.visionApi!!.visionApiKey
            VISION_ENDPOINT = appSecrets.visionApi!!.visionApiEndpoint
        }

        override fun doInBackground(vararg params: InputStream?): AzureOcrResult? {
            if (Strings.isEmptyOrWhitespace(VISION_KEY) || Strings.isEmptyOrWhitespace(VISION_ENDPOINT)) {
                return null
            }

            return to<Any?>(
                Runnable { runOcr(params[0]!!) },
                Logger("Azure Ocr Service")
            )
                .logIfError("Error running OCR")
                .get()
                .orElseGet(null)
        }

        @Throws(IOException::class)
        fun runOcr(imageStream: InputStream): AzureOcrResult? {
            val imageBytes = ByteArray(imageStream.available())
            imageStream.read(imageBytes)

            val url =
                URL(VISION_ENDPOINT + "/computervision/imageanalysis:analyze?api-version=2023-02-01-preview&features=read&language=en")
            val connection: HttpURLConnection = url.openConnection() as HttpsURLConnection
            connection.setRequestMethod("POST")
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", VISION_KEY)
            connection.setRequestProperty("Content-Type", "application/octet-stream")
            connection.setDoOutput(true)

            connection.getOutputStream().use { os ->
                os.write(imageBytes, 0, imageBytes.size)
            }
            val responseCode = connection.getResponseCode()
            if (responseCode == 200) {
                BufferedReader(InputStreamReader(connection.getInputStream(), "utf-8")).use { br ->
                    val response = StringBuilder()
                    var responseLine: String?
                    while ((br.readLine().also { responseLine = it }) != null) {
                        response.append(responseLine!!.trim { it <= ' ' })
                    }
                    return om.readValue<AzureOcrResult?>(response.toString(), AzureOcrResult::class.java)
                }
            } else {
                throw RuntimeException("Error response code: " + responseCode)
            }
        }

        override fun onPostExecute(result: AzureOcrResult?) {
            if (callback != null) {
                callback.onResult(result)
            }
        }
    }

    interface OcrCallback {
        fun onResult(result: AzureOcrResult?)
    }
}
