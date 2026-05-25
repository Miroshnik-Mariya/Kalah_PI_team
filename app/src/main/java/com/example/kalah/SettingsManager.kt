package com.example.kalah

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREF_NAME = "game_settings"
    private const val KEY_BOARD_STYLE = "board_style" // "wood" или "metal"

    fun saveBoardStyle(context: Context, style: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BOARD_STYLE, style).apply()
    }

    fun getBoardStyle(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BOARD_STYLE, "wood") ?: "wood"
    }

    // Добавьте в SettingsManager
    private const val KEY_MUSIC_ON = "music_on"

    fun saveMusicOn(context: Context, isOn: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MUSIC_ON, isOn).apply()
    }

    fun isMusicOn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MUSIC_ON, true)
    }
}