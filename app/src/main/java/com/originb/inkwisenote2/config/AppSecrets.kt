package com.originb.inkwisenote2.config

import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.originb.inkwisenote2.BuildConfig
import com.originb.inkwisenote2.common.Strings
import java.io.InputStream
import java.io.InputStreamReader

@AllArgsConstructor
@NoArgsConstructor
class AppSecrets {
    var visionApi: VisionApi? = null

    @AllArgsConstructor
    @NoArgsConstructor
    class VisionApi {
        var visionApiKey: String? = null
        var visionApiEndpoint: String? = null
    }

    val isAzureOcrEnabled: Boolean
        get() = Strings.isNotEmpty(visionApi!!.visionApiEndpoint) &&
                Strings.isNotEmpty(visionApi!!.visionApiKey)

    override fun toString(): String {
        return visionApi!!.visionApiKey + " | " + visionApi!!.visionApiEndpoint
    }

    companion object {
        private val om = ObjectMapper()

        fun loadFromEnv(): AppSecrets {
            val visionApi = VisionApi(BuildConfig.VISION_API_KEY, BuildConfig.VISION_API_ENDPOINT)
            return AppSecrets(visionApi)
        }

        fun loadFromInputStream(`is`: InputStream?): AppSecrets {
            try {
                val reader = InputStreamReader(`is`, "UTF-8")
                val appSecrets = om.readValue(reader, AppSecrets::class.java)
                reader.close()
                return appSecrets
            } catch (ex: Exception) {
                Log.e("AppSecrets", "Failed to load from the input stream", ex)
            }

            return AppSecrets(VisionApi())
        }
    }
}
