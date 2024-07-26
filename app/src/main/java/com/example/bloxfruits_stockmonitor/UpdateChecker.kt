package com.example.bloxfruits_stockmonitor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import android.app.DownloadManager
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

class UpdateChecker(private val context: Context) {

    private val githubApiUrl = "https://api.github.com/repos/MatiYT14/BloxFruits_StockMonitor/releases/latest"

    fun checkForUpdate() {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(githubApiUrl).build()
            val response = client.newCall(request).execute()
            val jsonResponse = response.body?.string()

            jsonResponse?.let {
                val jsonObject = JSONObject(it)
                val latestVersion = jsonObject.getString("tag_name")
                val downloadUrl = jsonObject.getJSONArray("assets")
                    .getJSONObject(0)
                    .getString("browser_download_url")

                val currentVersion = "1.0.0" // Zamień na aktualną wersję swojej aplikacji

                if (latestVersion != currentVersion) {
                    downloadAndInstallApk(downloadUrl)
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateChecker", "Error checking for updates", e)
        }
    }

    private fun downloadAndInstallApk(downloadUrl: String) {
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("BloxFruits Stock Monitor Update")
            .setDescription("Downloading update...")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update.apk")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        // Monitorowanie zakończenia pobierania i instalacja
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "update.apk")
            ),
            "application/vnd.android.package-archive"
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }
}