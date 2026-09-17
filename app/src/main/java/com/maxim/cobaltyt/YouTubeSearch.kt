package com.maxim.cobaltyt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class YouTubeSearch(private val store: LocalStore) {
    private val client = OkHttpClient()

    suspend fun search(query: String): Result<List<VideoItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val key = store.youtubeKey()
            require(key.isNotBlank()) { "Add a YouTube Data API v3 key in Settings first." }

            val url = "https://www.googleapis.com/youtube/v3/search" +
                    "?part=snippet&type=video&maxResults=20&q=${java.net.URLEncoder.encode(query, "UTF-8")}&key=$key"

            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("YouTube API HTTP ${response.code}: $body")

                val root = JSONObject(body)
                val items = root.getJSONArray("items")
                buildList {
                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val id = item.getJSONObject("id").getString("videoId")
                        val snippet = item.getJSONObject("snippet")
                        val thumbs = snippet.getJSONObject("thumbnails")
                        val thumb = when {
                            thumbs.has("high") -> thumbs.getJSONObject("high").getString("url")
                            thumbs.has("medium") -> thumbs.getJSONObject("medium").getString("url")
                            else -> thumbs.getJSONObject("default").getString("url")
                        }
                        add(
                            VideoItem(
                                id = id,
                                title = snippet.getString("title"),
                                channel = snippet.getString("channelTitle"),
                                thumbnail = thumb,
                                youtubeUrl = "https://www.youtube.com/watch?v=$id"
                            )
                        )
                    }
                }
            }
        }
    }
}
