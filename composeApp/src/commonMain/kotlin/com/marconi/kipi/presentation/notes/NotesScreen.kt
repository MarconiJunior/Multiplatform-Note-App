package com.marconi.kipi.presentation.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import androidx.navigation.NavController
import com.marconi.kipi.presentation.notes.components.NoteItem
import com.marconi.kipi.presentation.notes.components.OrderSection
import com.marconi.kipi.navigation.Screen
import kipi.composeapp.generated.resources.Res
import kipi.composeapp.generated.resources.all_notes
import kipi.composeapp.generated.resources.delete_note
import kipi.composeapp.generated.resources.note_delete_confirmation_no
import kipi.composeapp.generated.resources.note_delete_confirmation_question
import kipi.composeapp.generated.resources.note_delete_confirmation_yes
import kipi.composeapp.generated.resources.notes_length
import kipi.composeapp.generated.resources.your_note
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material3.FloatingActionButton
import kipi.composeapp.generated.resources.add_note
import androidx.compose.material.icons.filled.Add

@Composable
fun NotesScreen(
    navController: NavController,
    viewModel: NotesViewModel = koinViewModel()
) {
    val state = viewModel.state.value
    Scaffold(
        topBar = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(Res.string.all_notes),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = pluralStringResource(
                        Res.plurals.notes_length,
                        state.notes.size,
                        state.notes.size
                    ),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddEditNoteScreen.route)
                },
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.add_note)
                )
            }
        },
    ) { paddingValues ->
        val showDialog by viewModel.showDialog.collectAsState()
        if (showDialog) {
            DeleteConfirmDialog()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.your_note),
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(
                    onClick = {
                        viewModel.onEvent(NotesEvent.ToggleOrderSection)
                    },
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                }
            }
            AnimatedVisibility(
                visible = state.isOrderSectionVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                OrderSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    noteOrder = state.noteOrder,
                    onOrderChange = {
                        viewModel.onEvent(NotesEvent.Order(it))
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.notes) { note ->
                    NoteItem(
                        note = note,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(
                                    Screen.AddEditNoteScreen.route +
                                            "?noteId=${note.id}&noteColor=${note.color}"
                                )
                            },
                        onDeleteClick = {
                            viewModel.setCurrentSelectedNote(note)
                            viewModel.toggleDialogVisibility()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(viewModel: NotesViewModel = koinViewModel()) {
    AlertDialog(
        onDismissRequest = viewModel::toggleDialogVisibility,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(Res.string.delete_note))
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = stringResource(Res.string.delete_note)
                )
            }
        },
        text = { Text(text = stringResource(Res.string.note_delete_confirmation_question)) },
        confirmButton = {
            IconButton(onClick = viewModel::deleteNote) {
                Text(text = stringResource(Res.string.note_delete_confirmation_yes))
            }
        },
        dismissButton = {
            IconButton(onClick = viewModel::toggleDialogVisibility) {
                Text(text = stringResource(Res.string.note_delete_confirmation_no))
            }
        },
        shape = RoundedCornerShape(10.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        iconContentColor = MaterialTheme.colorScheme.onSurface
    )
}