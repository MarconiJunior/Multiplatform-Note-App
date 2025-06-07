package com.marconi.note

import androidx.compose.ui.window.ComposeUIViewController
import com.marconi.note.di.initializeKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initializeKoin() }
) { App() }