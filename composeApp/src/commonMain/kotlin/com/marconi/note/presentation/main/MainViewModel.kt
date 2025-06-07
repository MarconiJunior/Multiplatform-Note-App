package com.marconi.note.presentation.main

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marconi.note.presentation.util.ThemeManager
import com.marconi.note.snackbar_utils.SnackbarController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val themeManager: ThemeManager,
    private val snackbarController: SnackbarController
) : ViewModel() {
    private val _inDarkMode = MutableStateFlow<Boolean?>(false)
    val inDarkMode: StateFlow<Boolean?> = _inDarkMode

    private val _saveNoteCallback = MutableStateFlow {}
    val saveNoteCallback: StateFlow<() -> Unit> = _saveNoteCallback

    init {
        viewModelScope.launch {
            themeManager.themeFlow.collect { theme ->
                _inDarkMode.value = theme
            }
        }
    }

    fun setDarkMode(inDarkMode: Boolean?) {
        viewModelScope.launch {
            themeManager.setDarkThemeEnabled(inDarkMode)
        }
    }

    fun setSaveNoteCallback(callback: () -> Unit) {
        _saveNoteCallback.value = callback
    }

    fun saveNote() {
        viewModelScope.launch {
            saveNoteCallback.value.invoke()
        }
    }
}