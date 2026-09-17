package com.maxim.cobaltyt

data class VideoItem(
    val id: String,
    val title: String,
    val channel: String,
    val thumbnail: String,
    val youtubeUrl: String,
    val duration: String = ""
)

data class HistoryItem(
    val id: String,
    val title: String,
    val channel: String,
    val youtubeUrl: String,
    val mediaUrl: String,
    val watchedAt: Long
)
