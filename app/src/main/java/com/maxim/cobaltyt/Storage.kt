package com.maxim.cobaltyt

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("cobaltyt", Context.MODE_PRIVATE)

    fun cobaltUrl(): String =
        prefs.getString("cobalt_url", "http://10.0.2.2:9000/") ?: ""

    fun setCobaltUrl(value: String) {
        prefs.edit().putString("cobalt_url", value.trim().let { if (it.endsWith("/")) it else "$it/" }).apply()
    }

    fun youtubeKey(): String = prefs.getString("youtube_key", "") ?: ""

    fun setYoutubeKey(value: String) {
        prefs.edit().putString("youtube_key", value.trim()).apply()
    }

    fun history(): List<HistoryItem> {
        val raw = prefs.getString("history", "[]") ?: "[]"
        val a = JSONArray(raw)
        return buildList {
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                add(
                    HistoryItem(
                        o.getString("id"),
                        o.getString("title"),
                        o.optString("channel"),
                        o.getString("youtubeUrl"),
                        o.getString("mediaUrl"),
                        o.getLong("watchedAt")
                    )
                )
            }
        }.sortedByDescending { it.watchedAt }
    }

    fun addHistory(item: HistoryItem) {
        val list = history().filterNot { it.id == item.id }.toMutableList()
        list.add(0, item)
        val a = JSONArray()
        list.take(100).forEach {
            a.put(JSONObject().apply {
                put("id", it.id)
                put("title", it.title)
                put("channel", it.channel)
                put("youtubeUrl", it.youtubeUrl)
                put("mediaUrl", it.mediaUrl)
                put("watchedAt", it.watchedAt)
            })
        }
        prefs.edit().putString("history", a.toString()).apply()
    }

    fun clearHistory() {
        prefs.edit().putString("history", "[]").apply()
    }
}
