package com.marconi.note

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.marconi.note.navigation.SetupNavGraph
import com.marconi.note.ui.theme.NoteAppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    NoteAppTheme {
        val navController = rememberNavController()
        SetupNavGraph(navController)
    }
}