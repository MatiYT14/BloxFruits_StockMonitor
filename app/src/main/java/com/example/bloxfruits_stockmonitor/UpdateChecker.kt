package com.example.bloxfruits_stockmonitor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class UpdateChecker(private val context: Context) {

    private val githubApiUrl = "https://api.github.com/repos/<username>/<repository>/releases/latest" // Zamień na swój URL

    fun checkForUpdate() {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(githubApiUrl).build()
            val response = client.newCall(request).execute()
            val jsonResponse = response.body.string()

            jsonResponse?.let {
                val jsonObject = JSONObject(it)
                val latestVersion = jsonObject.getString("tag_name")
                val releaseNotes = jsonObject.getString("body")

                val currentVersion = "1.0.0" // Zamień na aktualną wersję swojej aplikacji

                if (latestVersion != currentVersion) {
                    showUpdateDialog(latestVersion, releaseNotes)
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateChecker", "Error checking for updates", e)
        }
    }

    private fun showUpdateDialog(version: String, releaseNotes: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://github.com/<username>/<repository>/releases/latest") // Zamień na swój URL
        }

        AlertDialog.Builder(context)
            .setTitle("Update Available")
            .setMessage("A new version ($version) is available.\n\nRelease Notes:\n$releaseNotes")
            .setPositiveButton("Update") { _, _ ->
                context.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}