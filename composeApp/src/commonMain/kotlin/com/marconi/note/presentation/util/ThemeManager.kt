package com.marconi.note.presentation.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeManager(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme_enabled")
    }

    val themeFlow: Flow<Boolean?> = dataStore.data.map { prefs ->
        if (prefs.contains(DARK_THEME_KEY)) prefs[DARK_THEME_KEY] else null
    }

    suspend fun setDarkThemeEnabled(value: Boolean?) {
        dataStore.edit { prefs ->
            if (value == null) {
                prefs -= DARK_THEME_KEY
            } else {
                prefs[DARK_THEME_KEY] = value
            }
        }
    }
}
