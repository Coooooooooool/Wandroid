package com.eric.wandroid.data.history

import android.content.Context
import android.content.SharedPreferences
import com.eric.wandroid.domain.model.ReadingHistoryItem
import com.eric.wandroid.domain.model.SearchHistoryItem

class HistoryRepository private constructor(context: Context) {
    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getSearchHistory(): List<SearchHistoryItem> {
        return preferences.getString(KEY_SEARCH_HISTORY, null)
            .orEmpty()
            .lineSequence()
            .mapNotNull(::decodeSearchHistory)
            .toList()
    }

    fun saveSearchKeyword(keyword: String) {
        val normalized = keyword.trim()
        if (normalized.isBlank()) return

        val updated = buildList {
            add(SearchHistoryItem(keyword = normalized, timestamp = System.currentTimeMillis()))
            addAll(getSearchHistory().filterNot { it.keyword.equals(normalized, ignoreCase = true) })
        }.take(MAX_SEARCH_HISTORY)

        preferences.edit().putString(
            KEY_SEARCH_HISTORY,
            updated.joinToString("\n", transform = ::encodeSearchHistory)
        ).apply()
    }

    fun removeSearchKeyword(keyword: String) {
        val updated = getSearchHistory().filterNot { it.keyword == keyword }
        preferences.edit().putString(
            KEY_SEARCH_HISTORY,
            updated.joinToString("\n", transform = ::encodeSearchHistory)
        ).apply()
    }

    fun clearSearchHistory() {
        preferences.edit().remove(KEY_SEARCH_HISTORY).apply()
    }

    fun getReadingHistory(): List<ReadingHistoryItem> {
        return preferences.getString(KEY_READING_HISTORY, null)
            .orEmpty()
            .lineSequence()
            .mapNotNull(::decodeReadingHistory)
            .toList()
    }

    fun saveReadingHistory(title: String, url: String) {
        val normalizedUrl = url.trim()
        if (normalizedUrl.isBlank()) return

        val normalizedTitle = title.trim().ifBlank { normalizedUrl }
        val updated = buildList {
            add(
                ReadingHistoryItem(
                    title = normalizedTitle,
                    url = normalizedUrl,
                    timestamp = System.currentTimeMillis()
                )
            )
            addAll(getReadingHistory().filterNot { it.url == normalizedUrl })
        }.take(MAX_READING_HISTORY)

        preferences.edit().putString(
            KEY_READING_HISTORY,
            updated.joinToString("\n", transform = ::encodeReadingHistory)
        ).apply()
    }

    fun clearReadingHistory() {
        preferences.edit().remove(KEY_READING_HISTORY).apply()
    }

    private fun encodeSearchHistory(item: SearchHistoryItem): String {
        return "${item.timestamp}\t${escape(item.keyword)}"
    }

    private fun decodeSearchHistory(raw: String): SearchHistoryItem? {
        val parts = raw.split('\t', limit = 2)
        if (parts.size < 2) return null
        return SearchHistoryItem(
            keyword = unescape(parts[1]),
            timestamp = parts[0].toLongOrNull() ?: return null
        )
    }

    private fun encodeReadingHistory(item: ReadingHistoryItem): String {
        return "${item.timestamp}\t${escape(item.title)}\t${escape(item.url)}"
    }

    private fun decodeReadingHistory(raw: String): ReadingHistoryItem? {
        val parts = raw.split('\t', limit = 3)
        if (parts.size < 3) return null
        return ReadingHistoryItem(
            title = unescape(parts[1]),
            url = unescape(parts[2]),
            timestamp = parts[0].toLongOrNull() ?: return null
        )
    }

    private fun escape(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\t", "\\t")
            .replace("\n", "\\n")
    }

    private fun unescape(value: String): String {
        return value
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\\\", "\\")
    }

    companion object {
        private const val PREF_NAME = "wandroid_history"
        private const val KEY_SEARCH_HISTORY = "search_history"
        private const val KEY_READING_HISTORY = "reading_history"
        private const val MAX_SEARCH_HISTORY = 20
        private const val MAX_READING_HISTORY = 100

        @Volatile
        private var instance: HistoryRepository? = null

        fun getInstance(context: Context): HistoryRepository {
            return instance ?: synchronized(this) {
                instance ?: HistoryRepository(context).also { instance = it }
            }
        }
    }
}
