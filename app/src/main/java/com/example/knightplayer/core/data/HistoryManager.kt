package com.example.knightplayer.core.data

import android.content.Context
import com.example.knightplayer.WatchHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object HistoryManager {

    private const val PREF_NAME = "watch_history"
    private const val KEY_HISTORY = "history"

    // 🔥 REACTIVE STATE: This tells the UI to instantly update when history changes!
    private val _historyFlow = MutableStateFlow<List<WatchHistory>>(emptyList())
    val historyFlow: StateFlow<List<WatchHistory>> = _historyFlow.asStateFlow()

    private var isInitialized = false

    /////////////////////////////////////////////////////////
    // 🚀 INITIALIZE (Call once when app starts)
    /////////////////////////////////////////////////////////
    fun init(context: Context) {
        if (!isInitialized) {
            _historyFlow.value = getHistoryFromPrefs(context)
            isInitialized = true
        }
    }

    /////////////////////////////////////////////////////////
    // 💾 SAVE / UPDATE HISTORY
    /////////////////////////////////////////////////////////
    fun saveHistory(context: Context, history: WatchHistory) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val list = getHistoryFromPrefs(context).toMutableList()

        // 🔥 Removes duplicate entry matching the EXACT String ID
        list.removeAll { it.videoId == history.videoId }

        // Add newest at top
        list.add(0, history)

        // Save to permanent storage
        prefs.edit().putString(KEY_HISTORY, gson.toJson(list)).apply()

        // Update the Flow so UI updates instantly
        _historyFlow.value = list
    }

    /////////////////////////////////////////////////////////
    // 📜 GET INTERNAL HISTORY
    /////////////////////////////////////////////////////////
    private fun getHistoryFromPrefs(context: Context): List<WatchHistory> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<WatchHistory>>() {}.type

        return try {
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Standard getter for direct access
    fun getHistory(context: Context): List<WatchHistory> = getHistoryFromPrefs(context)

    /////////////////////////////////////////////////////////
    // ❌ REMOVE SINGLE HISTORY ITEM
    /////////////////////////////////////////////////////////
    fun removeHistory(context: Context, videoId: String) { // 🔥 Changed to String
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val list = getHistoryFromPrefs(context).toMutableList()

        list.removeAll { it.videoId == videoId }

        prefs.edit().putString(KEY_HISTORY, Gson().toJson(list)).apply()
        _historyFlow.value = list
    }

    /////////////////////////////////////////////////////////
    // 🗑 CLEAR ALL HISTORY
    /////////////////////////////////////////////////////////
    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
        _historyFlow.value = emptyList()
    }
}