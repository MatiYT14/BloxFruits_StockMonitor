package com.example.bloxfruits_stockmonitor

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val channelId = "stock_monitor_channel"
    private val apiUrl = "https://fruityblox.com/stock"
    private var canSendNotifications = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        webView = findViewById(R.id.webView)
        webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                // Obsłuż błąd ładowania strony
            }
        }

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        webView.loadUrl(apiUrl)

        checkAndRequestNotificationPermissions()
        scheduleStockWork()

        // Sprawdź aktualizacje
        UpdateChecker(this).checkForUpdate()
    }

    private fun checkAndRequestNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission()
            } else {
                canSendNotifications = true
            }
        } else {
            canSendNotifications = true
        }
    }

    @SuppressLint("InlinedApi")
    private fun requestNotificationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            canSendNotifications = true
        } else {
            canSendNotifications = false
            showNoNotificationPermissionMessage()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Stock Monitor Channel"
            val descriptionText = "Channel for stock monitor notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNoNotificationPermissionMessage() {
        Toast.makeText(this, "Notification permissions are not granted. Some features may be limited.", Toast.LENGTH_LONG).show()
    }

    private fun scheduleStockWork() {
        val normalWorkRequest = PeriodicWorkRequestBuilder<StockWorker>(4, TimeUnit.HOURS)
            .setInitialDelay(0, TimeUnit.MINUTES)
            .build()

        val mirageWorkRequest = PeriodicWorkRequestBuilder<StockWorker>(2, TimeUnit.HOURS)
            .setInitialDelay(0, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "normalStockWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            normalWorkRequest
        )

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "mirageStockWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            mirageWorkRequest
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}