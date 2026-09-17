package com.maxim.cobaltyt

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

class DownloadHelper(private val context: Context) {
    fun enqueue(url: String, title: String): Long {
        val safe = title.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(80)
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(safe)
            .setDescription("Cobalt YT offline video")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Cobalt YT/$safe.mp4")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
        return (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }
}
