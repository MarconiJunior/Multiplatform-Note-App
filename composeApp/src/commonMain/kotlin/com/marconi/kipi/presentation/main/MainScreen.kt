package com.marconi.kipi.presentation.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marconi.kipi.events.CommonEvents
import com.marconi.kipi.events.utils.ObserveAsEvents
import com.marconi.kipi.presentation.add_edit_note.AddEditNoteScreen
import com.marconi.kipi.presentation.notes.NotesScreen
import com.marconi.kipi.presentation.util.Screen
import com.marconi.kipi.ui.theme.NoteAppTheme
import noteapp.composeapp.generated.resources.Res
import noteapp.composeapp.generated.resources.add_note
import noteapp.composeapp.generated.resources.dark_mode
import noteapp.composeapp.generated.resources.light_mode
import noteapp.composeapp.generated.resources.notes
import noteapp.composeapp.generated.resources.system_default
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    commonEvents: CommonEvents,
    viewModel: MainViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val navController = rememberNavController()
    val inDarkMode by viewModel.inDarkMode.collectAsState()
    val currentRoute = navController.currentBackStackEntryAsState()

    ObserveAsEvents(
        flow = commonEvents.events
    ) { event ->
        when (event) {
            is CommonEvents.Event.SaveNote -> {
                viewModel.setSaveNoteCallback(event.saveNote)
            }
        }
    }

    NoteAppTheme(
        darkTheme = when (inDarkMode) {
            true -> true
            false -> false
            null -> isSystemInDarkTheme()
        }
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (
                                currentRoute.value?.destination?.route
                                    ?.contains(Screen.AddEditNoteScreen.route) == false
                            ) {
                                navController.navigate(Screen.AddEditNoteScreen.route)
                            } else {
                                viewModel.saveNote()
                            }
                        },
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector =
                            if (
                                currentRoute.value?.destination?.route
                                    ?.contains(Screen.AddEditNoteScreen.route) == false
                            ) {
                                Icons.Default.Add
                            } else Icons.Default.Save,
                            contentDescription = stringResource(Res.string.add_note)
                        )
                    }
                },
                snackbarHost = {
                    SnackbarHost(snackbarHostState)
                },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = stringResource(Res.string.notes))
                        },
                        actions = {
                            var expanded by remember { mutableStateOf(false) }

                            Box {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(
                                        imageVector = when (inDarkMode) {
                                            true -> Icons.Default.DarkMode
                                            false -> Icons.Default.LightMode
                                            null -> Icons.Default.Settings
                                        },
                                        contentDescription = null
                                    )
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.light_mode)) },
                                        onClick = {
                                            viewModel.setDarkMode(false)
                                            expanded = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.LightMode, contentDescription = null)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.dark_mode)) },
                                        onClick = {
                                            viewModel.setDarkMode(true)
                                            expanded = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.DarkMode, contentDescription = null)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.system_default)) },
                                        onClick = {
                                            viewModel.setDarkMode(null)
                                            expanded = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Settings, contentDescription = null)
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.NotesScreen.route,
                    modifier = Modifier.padding(
                        PaddingValues(
                            top = paddingValues.calculateTopPadding(),
                            bottom = paddingValues.calculateBottomPadding(),
                            start = 16.dp,
                            end = 16.dp
                        )
                    )
                ) {
                    composable(route = Screen.NotesScreen.route) {
                        NotesScreen(navController = navController)
                    }
                    composable(
                        route = Screen.AddEditNoteScreen.route +
                                "?noteId={noteId}&noteColor={noteColor}",
                        arguments = listOf(
                            navArgument(
                                name = "noteId"
                            ) {
                                type = NavType.IntType
                                defaultValue = -1
                            },
                            navArgument(
                                name = "noteColor"
                            ) {
                                type = NavType.IntType
                                defaultValue = -1
                            },
                        )
                    ) {
                        val color = it.arguments?.getInt("noteColor") ?: -1
                        AddEditNoteScreen(
                            navController = navController,
                            noteColor = color
                        )
                    }
                }
            }
        }
    }
}