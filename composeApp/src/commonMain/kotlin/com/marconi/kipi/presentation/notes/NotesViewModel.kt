package com.marconi.kipi.presentation.notes

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marconi.kipi.domain.model.Note
import com.marconi.kipi.domain.use_case.NoteUseCases
import com.marconi.kipi.domain.util.NoteOrder
import com.marconi.kipi.snackbar_utils.SnackbarAction
import com.marconi.kipi.snackbar_utils.SnackbarController
import com.marconi.kipi.snackbar_utils.SnackbarEvent
import com.marconi.kipi.domain.util.OrderType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kipi.composeapp.generated.resources.Res
import kipi.composeapp.generated.resources.note_deleted

class NotesViewModel(
    private val noteUseCases: NoteUseCases,
    private val snackbarController: SnackbarController
) : ViewModel() {
    private val _state = mutableStateOf(NotesState())
    val state: State<NotesState> = _state

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog

    private val _currentSelectedNote = MutableStateFlow<Note?>(null)
    val currentSelectedNote: StateFlow<Note?> = _currentSelectedNote

    private var recentlyDeletedNote: Note? = null

    private var getNotesJob: Job? = null

    init {
        getNotes(NoteOrder.Date(OrderType.Descending))
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.Order -> {
                if (
                    state.value.noteOrder::class == event.noteOrder::class &&
                    state.value.noteOrder.orderType == event.noteOrder.orderType
                ) {
                    return
                }
                getNotes(event.noteOrder)
            }

            is NotesEvent.DeleteNote -> {
                viewModelScope.launch {
                    noteUseCases.deleteNote(event.note)
                    recentlyDeletedNote = event.note
                }
            }

            is NotesEvent.RestoreNote -> {
                viewModelScope.launch {
                    noteUseCases.addNote(recentlyDeletedNote ?: return@launch)
                    recentlyDeletedNote = null
                }
            }

            is NotesEvent.ToggleOrderSection -> {
                _state.value = state.value.copy(
                    isOrderSectionVisible = !state.value.isOrderSectionVisible
                )
            }
        }
    }

    private fun getNotes(noteOrder: NoteOrder) {
        getNotesJob?.cancel()
        getNotesJob = noteUseCases.getNotes(noteOrder)
            .onEach { notes ->
                _state.value = state.value.copy(
                    notes = notes,
                    noteOrder = noteOrder
                )
            }
            .launchIn(viewModelScope)
    }

    fun setCurrentSelectedNote(note: Note?) {
        _currentSelectedNote.value = note
    }

    fun toggleDialogVisibility() {
        _showDialog.value = !showDialog.value
    }

    fun deleteNote() {
        viewModelScope.launch {
            currentSelectedNote.value?.let { note ->
                onEvent(NotesEvent.DeleteNote(note))
                toggleDialogVisibility()
                setCurrentSelectedNote(null)
                snackbarController.sendEvent(
                    SnackbarEvent(
                        message = Res.string.note_deleted,
                        action = SnackbarAction(
                            "Undo",
                            action = { onEvent(NotesEvent.RestoreNote) }
                        )
                    )
                )
            }
        }
    }
}