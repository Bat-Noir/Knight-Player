package com.example.knightplayer.core.data

import android.content.Context

object SearchHistoryManager {

    private const val PREF_NAME = "search_history_pref"
    private const val KEY_HISTORY = "history"

    fun saveQuery(context: Context, query: String) {

        if (query.isBlank()) return

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val history = prefs.getStringSet(KEY_HISTORY, mutableSetOf())!!.toMutableSet()

        history.remove(query)
        history.add(query)

        prefs.edit().putStringSet(KEY_HISTORY, history).apply()
    }

    fun getHistory(context: Context): List<String> {

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        return prefs.getStringSet(KEY_HISTORY, setOf())!!.toList().reversed()
    }
}