package com.example.bloxfruits_stockmonitor

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class StockWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val client = OkHttpClient()
    private val notificationHelper = NotificationHelper(context)

    override suspend fun doWork(): Result {
        return try {
            val response = client.newCall(Request.Builder().url("https://fruityblox.com/stock").build()).execute()
            val html = response.body.string()
            val document = Jsoup.parse(html)

            val normalStockSection = document.select("h2:containsOwn(Normal Stock)").firstOrNull()?.parent()
            val normalStockNames = normalStockSection?.select("a")?.map { it.text() } ?: emptyList()

            val mirageStockSection = document.select("h2:containsOwn(Mirage Stock)").firstOrNull()?.parent()
            val mirageStockNames = mirageStockSection?.select("a")?.map { it.text() } ?: emptyList()

            val normalStockChanged = checkStockChanged("normalStock", normalStockNames)
            val mirageStockChanged = checkStockChanged("mirageStock", mirageStockNames)

            if (normalStockChanged) {
                notificationHelper.sendNotification("Normal Stock", normalStockNames)
            }
            if (mirageStockChanged) {
                notificationHelper.sendNotification("Mirage Stock", mirageStockNames)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("StockWorker", "Error fetching stock data", e)
            Result.failure()
        }
    }

    private fun checkStockChanged(key: String, newStock: List<String>): Boolean {
        val sharedPrefs = applicationContext.getSharedPreferences("stock_prefs", Context.MODE_PRIVATE)
        val oldStock = sharedPrefs.getStringSet(key, emptySet()) ?: emptySet()

        val newStockSet = newStock.toSet()
        val stockChanged = oldStock != newStockSet

        if (stockChanged) {
            sharedPrefs.edit().putStringSet(key, newStockSet).apply()
        }

        return stockChanged
    }
}