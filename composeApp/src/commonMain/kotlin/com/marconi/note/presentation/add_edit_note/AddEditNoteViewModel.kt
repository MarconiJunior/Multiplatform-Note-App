package com.marconi.note.presentation.add_edit_note

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marconi.note.domain.model.InvalidNoteException
import com.marconi.note.domain.model.Note
import com.marconi.note.domain.use_case.NoteUseCases
import com.marconi.note.events.CommonEvents
import com.marconi.note.snackbar_utils.SnackbarController
import com.marconi.note.snackbar_utils.SnackbarEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import noteapp.composeapp.generated.resources.Res
import noteapp.composeapp.generated.resources.*

class AddEditNoteViewModel(
    private val noteUseCases: NoteUseCases,
    private val commonEvents: CommonEvents,
    private val snackbarController: SnackbarController,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _noteTitle = mutableStateOf(
        NoteTextFieldState(
            hint = Res.string.enter_title
        )
    )
    val noteTitle: State<NoteTextFieldState> = _noteTitle

    private val _noteContent = mutableStateOf(
        NoteTextFieldState(
            hint = Res.string.enter_some_content
        )
    )
    val noteContent: State<NoteTextFieldState> = _noteContent

    private val _selectedCustomColor = MutableStateFlow<Color?>(null)
    val selectedCustomColor: StateFlow<Color?> = _selectedCustomColor

    private val _isColorDialogVisible = MutableStateFlow(false)
    val isColorDialogVisible: StateFlow<Boolean> = _isColorDialogVisible

    private val _isFontDialogVisible = MutableStateFlow(false)
    val isFontDialogVisible: StateFlow<Boolean> = _isFontDialogVisible

    private val _noteColor = MutableStateFlow(Note.noteColors.random().toArgb())
    val noteColor: StateFlow<Int> = _noteColor

    private val _fontSize = MutableStateFlow(16f)
    val fontSize: StateFlow<Float> = _fontSize

    private val _textColor = MutableStateFlow(Color.Black.toArgb())
    val textColor: StateFlow<Int> = _textColor

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    private var currentNoteId: Int? = null


    init {
        saveNote()
        savedStateHandle.get<Int>("noteId")?.let { noteId ->
            if(noteId != -1) {
                viewModelScope.launch {
                    noteUseCases.getNote(noteId)?.also { note ->
                        currentNoteId = note.id
                        _noteTitle.value = noteTitle.value.copy(
                            text = note.title,
                            isHintVisible = false
                        )
                        _noteContent.value = _noteContent.value.copy(
                            text = note.content,
                            isHintVisible = false
                        )
                        onEvent(AddEditNoteEvent.ChangeColor(note.color))
                        onEvent(AddEditNoteEvent.ChangeFontSize(note.fontSize))
                        onEvent(AddEditNoteEvent.ChangeTextColor(note.textColor))
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditNoteEvent) {
        when (event) {
            is AddEditNoteEvent.EnteredTitle -> {
                _noteTitle.value = noteTitle.value.copy(
                    text = event.value
                )
            }
            is AddEditNoteEvent.ChangeTitleFocus -> {
                _noteTitle.value = noteTitle.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            noteTitle.value.text.isBlank()
                )
            }
            is AddEditNoteEvent.EnteredContent -> {
                _noteContent.value = _noteContent.value.copy(
                    text = event.value
                )
            }
            is AddEditNoteEvent.ChangeContentFocus -> {
                _noteContent.value = _noteContent.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            _noteContent.value.text.isBlank()
                )
            }
            is AddEditNoteEvent.ChangeColor -> {
                _noteColor.value = event.color
            }
            is AddEditNoteEvent.ChangeFontSize -> {
                _fontSize.value = event.fontSize
            }
            is AddEditNoteEvent.ChangeTextColor -> {
                _textColor.value = event.textColor
            }
            is AddEditNoteEvent.SaveNote -> {
                viewModelScope.launch {
                    try {
                        noteUseCases.addNote(
                            Note(
                                title = noteTitle.value.text,
                                content = noteContent.value.text,
                                timestamp = Clock.System.now().epochSeconds,
                                color = noteColor.value,
                                textColor = textColor.value,
                                fontSize = fontSize.value,
                                id = currentNoteId
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveNote)
                    } catch(e: InvalidNoteException) {
                        emmitSnackbar(e.message)
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data object SaveNote: UiEvent()
    }

    private fun saveNote() {
        viewModelScope.launch {
            commonEvents.emitEvent(CommonEvents.Event.SaveNote {
                onEvent(AddEditNoteEvent.SaveNote)
            })
        }
    }

    private suspend fun emmitSnackbar(message: String?) {
        snackbarController.sendEvent(
            SnackbarEvent(
                message = message ?: Res.string.couldn_t_save_note
            )
        )
    }

    fun setSelectedCustomColor(color: Color) {
        _selectedCustomColor.value = color
    }

    fun toggleColorDialogVisibility() {
        _isColorDialogVisible.value = !(isColorDialogVisible.value ?: false)
    }

    fun toggleFontDialogVisibility() {
        _isFontDialogVisible.value = !(isFontDialogVisible.value ?: false)
    }
}
