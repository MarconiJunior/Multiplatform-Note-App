package com.marconi.note.presentation.util

import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual class ThemeManager {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual fun getSavedTheme(): ThemeSettings? {
        val savedData = userDefaults.stringForKey(THEME_SETTINGS_KEY)
        return if (savedData != null) {
            Json.decodeFromString(savedData)
        } else {
            null
        }
    }

    actual fun setDarkThemeEnabled(themeSettings: ThemeSettings) {
        val encodedSettings = Json.encodeToString(themeSettings)
        userDefaults.setObject(encodedSettings, forKey = THEME_SETTINGS_KEY)
    }
}
