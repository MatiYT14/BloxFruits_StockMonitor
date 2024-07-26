package com.example.bloxfruits_stockmonitor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {

    private val channelId = "stock_monitor_channel"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Stock Monitor Channel"
            val descriptionText = "Channel for stock monitor notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(stockType: String, stockNames: List<String>) {
        val stockNamesText = if (stockNames.isNotEmpty()) {
            "${stockType}:\n${stockNames.joinToString("\n")}"
        } else {
            ""
        }

        val notificationText = "$stockType has updated! (${stockNames.size} fruits)"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Stock Update")
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(stockNamesText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            with(NotificationManagerCompat.from(context)) {
                try {
                    notify(stockType.hashCode(), builder.build())
                } catch (e: Exception) {
                    // Obsłuż błąd wysyłania powiadomienia
                }
            }
        }
    }
}