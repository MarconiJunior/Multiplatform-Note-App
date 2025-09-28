package com.marconi.kipi.presentation.add_edit_note

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marconi.kipi.domain.model.InvalidNoteException
import com.marconi.kipi.domain.model.Note
import com.marconi.kipi.domain.use_case.NoteUseCases
import com.marconi.kipi.events.CommonEvents
import com.marconi.kipi.navigation.Navigator
import com.marconi.kipi.rich_text.styles.Style
import com.marconi.kipi.rich_text.styles.StyleRange
import com.marconi.kipi.rich_text.hasStyleInRange
import com.marconi.kipi.rich_text.optimizeStyles
import com.marconi.kipi.rich_text.removeStylesInRange
import com.marconi.kipi.rich_text.serializeStyles
import com.marconi.kipi.rich_text.deserializeStyles
import com.marconi.kipi.snackbar_utils.SnackbarController
import com.marconi.kipi.snackbar_utils.SnackbarEvent
import com.marconi.kipi.ui.theme.FontTypes
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kipi.composeapp.generated.resources.Res
import kipi.composeapp.generated.resources.*

class AddEditNoteViewModel(
    private val noteUseCases: NoteUseCases,
    private val snackbarController: SnackbarController,
    private val navigator: Navigator,
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

    private val _uiState = MutableStateFlow(RichTextUiState())
    val uiState: StateFlow<RichTextUiState> = _uiState

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

    private val _hasSelection = MutableStateFlow(false)
    val hasSelection: StateFlow<Boolean> = _hasSelection

    private val _activeStyles = MutableStateFlow<Set<Style>>(emptySet())
    val activeStyles: StateFlow<Set<Style>> = _activeStyles

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
                        note.richTextStyles?.let { stylesString ->
                            _uiState.value = _uiState.value.copy(
                                styles = deserializeStyles(stylesString)
                            )
                        }
                    }
                }
            }
        }
        setDefaultNoteColors()
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
            is AddEditNoteEvent.ToggleStyle -> {
                val selection = uiState.value.selection
                if (selection.start != selection.end) {
                    toggleStyleInSelection(event.style)
                } else {
                    val currentActiveStyles = _activeStyles.value.toMutableSet()
                    if (currentActiveStyles.contains(event.style)) {
                        currentActiveStyles.remove(event.style)
                    } else {
                        currentActiveStyles.add(event.style)
                    }
                    _activeStyles.value = currentActiveStyles
                }
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
                                richTextStyles = serializeStyles(_uiState.value.styles),
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

    private fun toggleStyleInSelection(style: Style) {
        val selection = uiState.value.selection
        if (selection.start == selection.end) return

        val currentStyles = uiState.value.styles.toMutableList()

        val hasStyle = currentStyles.hasStyleInRange(selection.start, selection.end, style)

        if (hasStyle) {
            val updatedStyles = currentStyles.removeStylesInRange(
                selection.start,
                selection.end,
                style
            )
            _uiState.value = uiState.value.copy(styles = updatedStyles.optimizeStyles())
        } else {
            currentStyles.add(
                StyleRange(
                    start = selection.start,
                    end = selection.end,
                    style = style
                )
            )
            _uiState.value = uiState.value.copy(styles = currentStyles.optimizeStyles())
        }
    }

    fun updateSelection(selection: TextRange) {
        _uiState.value = uiState.value.copy(selection = selection)
        _hasSelection.value = selection.start != selection.end

        if (selection.start != selection.end) {
            val activeStylesInSelection = uiState.value.styles
                .filter { style ->
                    style.start <= selection.start && style.end >= selection.end
                }
                .map { it.style }
                .toSet()

            _activeStyles.value = activeStylesInSelection
        } else {
            _activeStyles.value = emptySet()
        }
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
