package com.example.bloxfruits_stockmonitor

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class NormalStockWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId, R.layout.widget_normal_stock)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, widgetLayoutId: Int) {
        val views = RemoteViews(context.packageName, widgetLayoutId)

        // Uruchomienie zadania pobierania danych w tle
        CoroutineScope(Dispatchers.IO).launch {
            val data = fetchData("https://example.com/normal_stock")
            withContext(Dispatchers.Main) {
                // Aktualizacja widoków widgeta na podstawie pobranych danych
                views.setTextViewText(R.id.widget_normal_stock_title, data)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        // Stwórz intent do uruchomienia MainActivity po kliknięciu widgetu
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_normal_stock_title, pendingIntent)

        // Aktualizacja widgeta
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun fetchData(urlString: String): String {
        var result = ""
        try {
            val url = URL(urlString)
            val urlConnection = url.openConnection() as HttpURLConnection
            try {
                val inputStream = urlConnection.inputStream
                result = inputStream.bufferedReader().use { it.readText() }
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}