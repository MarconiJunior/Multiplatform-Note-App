package com.marconi.kipi.presentation.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.marconi.kipi.navigation.NavigationAction
import com.marconi.kipi.navigation.Navigator
import com.marconi.kipi.presentation.add_edit_note.AddEditNoteScreen
import com.marconi.kipi.presentation.notes.NotesScreen
import com.marconi.kipi.navigation.Screen
import com.marconi.kipi.ui.theme.NoteAppTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    commonEvents: CommonEvents,
    viewModel: MainViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val navController = rememberNavController()
    val navigator = koinInject<Navigator>()

    val inDarkMode by viewModel.inDarkMode.collectAsState()
    val currentRoute = navController.currentBackStackEntryAsState()

    ObserveAsEvents(flow = navigator.navigationActions) { action ->
        when (action) {
            is NavigationAction.Navigate -> navController.navigate(
                action.destination?.route ?: Screen.NotesScreen
            ) {
                action.navOptions(this)
            }

            NavigationAction.NavigateUp -> navController.navigateUp()
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
                snackbarHost = {
                    SnackbarHost(snackbarHostState)
                },
                topBar = {
//                    TopAppBar(
//                        title = {
//                            Text(text = stringResource(Res.string.notes))
//                        },
//                        actions = {
//                            var expanded by remember { mutableStateOf(false) }
//
//                            Box {
//                                IconButton(onClick = { expanded = true }) {
//                                    Icon(
//                                        imageVector = when (inDarkMode) {
//                                            true -> Icons.Default.DarkMode
//                                            false -> Icons.Default.LightMode
//                                            null -> Icons.Default.Settings
//                                        },
//                                        contentDescription = null
//                                    )
//                                }
//
//                                DropdownMenu(
//                                    expanded = expanded,
//                                    onDismissRequest = { expanded = false }
//                                ) {
//                                    DropdownMenuItem(
//                                        text = { Text(stringResource(Res.string.light_mode)) },
//                                        onClick = {
//                                            viewModel.setDarkMode(false)
//                                            expanded = false
//                                        },
//                                        leadingIcon = {
//                                            Icon(Icons.Default.LightMode, contentDescription = null)
//                                        }
//                                    )
//                                    DropdownMenuItem(
//                                        text = { Text(stringResource(Res.string.dark_mode)) },
//                                        onClick = {
//                                            viewModel.setDarkMode(true)
//                                            expanded = false
//                                        },
//                                        leadingIcon = {
//                                            Icon(Icons.Default.DarkMode, contentDescription = null)
//                                        }
//                                    )
//                                    DropdownMenuItem(
//                                        text = { Text(stringResource(Res.string.system_default)) },
//                                        onClick = {
//                                            viewModel.setDarkMode(null)
//                                            expanded = false
//                                        },
//                                        leadingIcon = {
//                                            Icon(Icons.Default.Settings, contentDescription = null)
//                                        }
//                                    )
//                                }
//                            }
//                        }
//                    )
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
                            noteColor = color
                        )
                    }
                }
            }
        }
    }
}