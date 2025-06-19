package com.marconi.kipi

import androidx.compose.ui.window.ComposeUIViewController
import com.marconi.kipi.di.initializeKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initializeKoin() }
) { App() }