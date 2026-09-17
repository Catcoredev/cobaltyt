package com.maxim.cobaltyt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class CobaltApi(private val store: LocalStore) {
    private val client = OkHttpClient()

    suspend fun resolve(youtubeUrl: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val json = JSONObject().apply {
                put("url", youtubeUrl)
                put("downloadMode", "auto")
                put("videoQuality", "1080")
                put("youtubeVideoCodec", "h264")
                put("youtubeVideoContainer", "mp4")
                put("filenameStyle", "basic")
            }

            val builder = Request.Builder()
                .url(store.cobaltUrl())
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")

            // Optional API-Key. Put it in cobalt URL as an authenticated reverse proxy
            // or extend LocalStore if your private instance requires one.
            client.newCall(builder.build()).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("Cobalt HTTP ${response.code}: $body")
                val obj = JSONObject(body)
                when (obj.optString("status")) {
                    "tunnel", "redirect" -> obj.getString("url")
                    "error" -> error(obj.optJSONObject("error")?.optString("code") ?: "Cobalt error")
                    "picker" -> error("This URL returned multiple media items; picker support is not enabled in this first build.")
                    "local-processing" -> error("Cobalt returned local-processing. Configure the instance to provide tunnel/redirect output.")
                    else -> error("Unknown Cobalt response.")
                }
            }
        }
    }
}
