package com.marconi.kipi

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.marconi.kipi.events.utils.ObserveAsEvents
import com.marconi.kipi.presentation.main.MainScreen
import com.marconi.kipi.snackbar_utils.SnackbarController
import com.marconi.kipi.ui.theme.NoteAppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    NoteAppTheme {
        val snackbarController: SnackbarController = koinInject()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        ObserveAsEvents(
            flow = snackbarController.events,
            snackbarHostState
        ) { event ->
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()

                val result = snackbarHostState.showSnackbar(
                    message = if (event.message is StringResource) {
                        getString(event.message)
                    } else {
                        event.message.toString()
                    },
                    actionLabel = event.action?.name,
                    duration = SnackbarDuration.Long
                )

                if(result == SnackbarResult.ActionPerformed) {
                    event.action?.action?.invoke()
                }
            }
        }
        MainScreen(
            koinInject(),
            snackbarHostState = snackbarHostState
        )
    }
}