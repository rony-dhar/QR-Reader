package com.qrreader.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val storage = Storage(app)

    var theme by mutableStateOf(storage.loadTheme())
        private set

    val history = mutableStateListOf<ScanRecord>().apply { addAll(storage.loadHistory()) }

    fun setTheme(value: AppTheme) {
        theme = value
        storage.saveTheme(value)
    }

    /** Adds a record at the top. Re-scanning the same content just moves it to the top. */
    fun add(record: ScanRecord) {
        history.removeAll { it.content == record.content && it.source == record.source }
        history.add(0, record)
        persist()
    }

    fun delete(id: Long) {
        history.removeAll { it.id == id }
        persist()
    }

    fun clearHistory() {
        history.clear()
        persist()
    }

    private fun persist() = storage.saveHistory(history.toList())
}
