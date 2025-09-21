package com.marconi.kipi.presentation.add_edit_note

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marconi.kipi.domain.model.InvalidNoteException
import com.marconi.kipi.domain.model.Note
import com.marconi.kipi.domain.use_case.NoteUseCases
import com.marconi.kipi.events.CommonEvents
import com.marconi.kipi.navigation.Navigator
import com.marconi.kipi.snackbar_utils.SnackbarController
import com.marconi.kipi.snackbar_utils.SnackbarEvent
import com.marconi.kipi.ui.theme.FontTypes
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kipi.composeapp.generated.resources.Res
import kipi.composeapp.generated.resources.*
data class NoteUiState(
    val title: String = "",
    val content: String = "",
    val backgroundColor: Color = Color.White,
    val textColor: Color = Color.Black,
    val fontSize: Int = 16,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderlined: Boolean = false,
    val textAlign: TextAlign = TextAlign.Start,
    val isLoading: Boolean = false
)

class AddEditNoteViewModel(
    private val noteUseCases: NoteUseCases,
    private val snackbarController: SnackbarController,
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    var uiState by mutableStateOf(NoteUiState())
        private set

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

    private val _defaultColors = MutableStateFlow(emptyList<Color>())
    val defaultColors: StateFlow<List<Color>> = _defaultColors

    private val _fontSize = MutableStateFlow(16f)
    val fontSize: StateFlow<Float> = _fontSize

    private val _textColor = MutableStateFlow(Color.Black.toArgb())
    val textColor: StateFlow<Int> = _textColor

    private val _fontFamily = MutableStateFlow(FontTypes.Domine)
    val fontFamily: StateFlow<FontTypes> = _fontFamily

    private val _colorExpanded = MutableStateFlow(false)
    val colorExpanded: StateFlow<Boolean> = _colorExpanded

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    private var currentNoteId: Int? = null

    init {
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
                        onEvent(
                            AddEditNoteEvent.ChangeFontFamily(
                                FontTypes.entries
                                    .find { it.value == note.fontStyle } ?: FontTypes.Domine
                            )
                        )
                        onEvent(AddEditNoteEvent.ChangeTextColor(note.textColor))
                    }
                }
            }
        }
        setDefaultNoteColors()
    }

    fun updateTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun updateContent(content: String) {
        uiState = uiState.copy(content = content)
    }

    fun updateBackgroundColor(color: Color) {
        uiState = uiState.copy(backgroundColor = color)
    }

    fun updateTextColor(color: Color) {
        uiState = uiState.copy(textColor = color)
    }

    fun updateFontSize(size: Int) {
        uiState = uiState.copy(fontSize = size)
    }

    fun toggleBold() {
        uiState = uiState.copy(isBold = !uiState.isBold)
    }

    fun toggleItalic() {
        uiState = uiState.copy(isItalic = !uiState.isItalic)
    }

    fun toggleUnderline() {
        uiState = uiState.copy(isUnderlined = !uiState.isUnderlined)
    }

    fun updateTextAlign(align: TextAlign) {
        uiState = uiState.copy(textAlign = align)
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
            is AddEditNoteEvent.ChangeFontFamily -> {
                _fontFamily.value = event.fontFamily
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
                                fontStyle = fontFamily.value.value,
                                id = currentNoteId
                            )
                        )
                        navigator.navigateUp()
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

    private fun setDefaultNoteColors() {
        viewModelScope.launch {
            val noteColors = noteUseCases.getNotesColors()
            Note.noteColors
            _defaultColors.emit(Note.noteColors + noteColors.map { Color(it) })
        }
    }

    private suspend fun emmitSnackbar(message: String?) {
        snackbarController.sendEvent(
            SnackbarEvent(
                message = message ?: Res.string.couldn_t_save_note
            )
        )
    }

    fun setColorExpanded(expanded: Boolean) {
        _colorExpanded.value = expanded
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
