package com.example.expensetracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.expensetracker.ui.theme.ThemeMode

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            "settings_prefs",
            Context.MODE_PRIVATE
        )

    private val themeKey = "theme_mode"

    fun getThemeMode(): ThemeMode {

        val value =
            prefs.getString(
                themeKey,
                ThemeMode.SYSTEM.storageValue
            )

        return ThemeMode.entries.firstOrNull {
            it.storageValue == value
        } ?: ThemeMode.SYSTEM
    }

    fun setThemeMode(mode: ThemeMode) {

        prefs.edit()
            .putString(themeKey, mode.storageValue)
            .apply()
    }
}