package com.qrreader.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class AppTheme(val label: String) {
    WHITE("White"),
    GREY("Grey"),
    DARK("Dark"),
}

enum class Source { SCANNED, CREATED }

data class ScanRecord(
    val id: Long,
    val content: String,
    val format: String,
    val kind: String,
    val source: Source,
    val timestamp: Long,
)

/** Tiny on-device store: theme + history, kept in SharedPreferences as JSON. */
class Storage(context: Context) {
    private val prefs = context.getSharedPreferences("qr_reader", Context.MODE_PRIVATE)

    fun loadTheme(): AppTheme =
        runCatching { AppTheme.valueOf(prefs.getString("theme", null) ?: AppTheme.WHITE.name) }
            .getOrDefault(AppTheme.WHITE)

    fun saveTheme(theme: AppTheme) {
        prefs.edit().putString("theme", theme.name).apply()
    }

    fun loadHistory(): List<ScanRecord> {
        val raw = prefs.getString("history", null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                ScanRecord(
                    id = o.getLong("id"),
                    content = o.getString("content"),
                    format = o.getString("format"),
                    kind = o.getString("kind"),
                    source = Source.valueOf(o.getString("source")),
                    timestamp = o.getLong("ts"),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun saveHistory(list: List<ScanRecord>) {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject()
                    .put("id", r.id)
                    .put("content", r.content)
                    .put("format", r.format)
                    .put("kind", r.kind)
                    .put("source", r.source.name)
                    .put("ts", r.timestamp)
            )
        }
        prefs.edit().putString("history", arr.toString()).apply()
    }
}
