package com.example.history

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class SearchHistoryItem(
    val queryOrUrl: String,
    val isUrl: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class SearchHistoryManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("r_browser_history_cache", Context.MODE_PRIVATE)

    fun saveSearch(queryOrUrl: String) {
        val trimmed = queryOrUrl.trim()
        if (trimmed.isBlank() || trimmed.startsWith("data:")) return

        val isUrl = trimmed.startsWith("http://") || trimmed.startsWith("https://") || (trimmed.contains(".") && !trimmed.contains(" "))
        val current = getHistory().toMutableList()

        // Remove if duplicate exists
        current.removeAll { it.queryOrUrl.equals(trimmed, ignoreCase = true) }

        // Prepend latest item
        current.add(0, SearchHistoryItem(trimmed, isUrl, System.currentTimeMillis()))

        // Keep maximum 50 recent items
        val limited = current.take(50)
        persistHistory(limited)
    }

    fun getHistory(): List<SearchHistoryItem> {
        val jsonStr = prefs.getString("history_items", null) ?: return getDefaultHistory()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<SearchHistoryItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    SearchHistoryItem(
                        queryOrUrl = obj.getString("q"),
                        isUrl = obj.getBoolean("u"),
                        timestamp = obj.optLong("t", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            getDefaultHistory()
        }
    }

    fun removeHistoryItem(queryOrUrl: String) {
        val current = getHistory().filterNot { it.queryOrUrl.equals(queryOrUrl, ignoreCase = true) }
        persistHistory(current)
    }

    fun clearAllHistory() {
        prefs.edit().remove("history_items").apply()
    }

    private fun persistHistory(items: List<SearchHistoryItem>) {
        try {
            val arr = JSONArray()
            items.forEach { item ->
                val obj = JSONObject()
                obj.put("q", item.queryOrUrl)
                obj.put("u", item.isUrl)
                obj.put("t", item.timestamp)
                arr.put(obj)
            }
            prefs.edit().putString("history_items", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun getDefaultHistory(): List<SearchHistoryItem> {
        return listOf(
            SearchHistoryItem("https://duckduckgo.com", true),
            SearchHistoryItem("https://developer.mozilla.org", true),
            SearchHistoryItem("https://github.com", true),
            SearchHistoryItem("https://httpbin.org/get", true),
            SearchHistoryItem("android compose devtools", false)
        )
    }
}
