package com.marconi.note.presentation.util

import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

actual class ThemeManager actual constructor(private val context: Context) : ThemeManager() {
    private val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    actual fun getSavedTheme(): ThemeSettings? {
        val savedData = sharedPreferences.getString(THEME_SETTINGS_KEY, null)
        return if (savedData != null) {
            Json.decodeFromString(savedData)
        } else {
            null
        }
    }

    actual fun setDarkThemeEnabled(themeSettings: ThemeSettings) {
        sharedPreferences.edit {
            putString(THEME_SETTINGS_KEY, Json.encodeToString(themeSettings))
        }
    }
}
