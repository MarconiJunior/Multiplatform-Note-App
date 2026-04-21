package com.marconi.kipi.presentation.add_edit_note

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marconi.kipi.domain.model.InvalidNoteException
import com.marconi.kipi.domain.model.Note
import com.marconi.kipi.domain.use_case.NoteUseCases
import com.marconi.kipi.navigation.Navigator
import com.marconi.kipi.rich_text.adjustForDeletion
import com.marconi.kipi.rich_text.adjustForInsertion
import com.marconi.kipi.rich_text.colorAtPosition
import com.marconi.kipi.rich_text.deserializeStyles
import com.marconi.kipi.rich_text.hasStyleInRange
import com.marconi.kipi.rich_text.optimizeStyles
import com.marconi.kipi.rich_text.removeStylesInRange
import com.marconi.kipi.rich_text.serializeStyles
import com.marconi.kipi.rich_text.stylesAtPosition
import com.marconi.kipi.rich_text.styles.Style
import com.marconi.kipi.rich_text.styles.StyleRange
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

    private val _noteTitle = mutableStateOf(NoteTextFieldState(hint = Res.string.enter_title))
    val noteTitle: State<NoteTextFieldState> = _noteTitle

    private val _noteContent = mutableStateOf(NoteTextFieldState(hint = Res.string.enter_some_content))
    val noteContent: State<NoteTextFieldState> = _noteContent

    private val _uiState = MutableStateFlow(RichTextUiState())
    val uiState: StateFlow<RichTextUiState> = _uiState

    private val _selectedCustomColor = MutableStateFlow<Color?>(null)
    val selectedCustomColor: StateFlow<Color?> = _selectedCustomColor

    private val _isColorDialogVisible = MutableStateFlow(false)
    val isColorDialogVisible: StateFlow<Boolean> = _isColorDialogVisible

    private val _isFontDialogVisible = MutableStateFlow(false)
    val isFontDialogVisible: StateFlow<Boolean> = _isFontDialogVisible

    private val _isSelectionColorDialogVisible = MutableStateFlow(false)
    val isSelectionColorDialogVisible: StateFlow<Boolean> = _isSelectionColorDialogVisible

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

    private val _activeSelectionColor = MutableStateFlow<Int?>(null)
    val activeSelectionColor: StateFlow<Int?> = _activeSelectionColor

    private var currentNoteId: Int? = null

    init {
        savedStateHandle.get<Int>("noteId")?.let { noteId ->
            if (noteId != -1) {
                viewModelScope.launch {
                    noteUseCases.getNote(noteId)?.also { note ->
                        currentNoteId = note.id
                        _noteTitle.value = noteTitle.value.copy(text = note.title, isHintVisible = false)
                        _noteContent.value = _noteContent.value.copy(text = note.content, isHintVisible = false)
                        onEvent(AddEditNoteEvent.ChangeColor(note.color))
                        onEvent(AddEditNoteEvent.ChangeFontSize(note.fontSize))
                        onEvent(AddEditNoteEvent.ChangeFontFamily(
                            FontTypes.entries.find { it.value == note.fontStyle } ?: FontTypes.Domine
                        ))
                        onEvent(AddEditNoteEvent.ChangeTextColor(note.textColor))
                        note.richTextStyles?.let { stylesString ->
                            _uiState.value = _uiState.value.copy(styles = deserializeStyles(stylesString))
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
                _noteTitle.value = noteTitle.value.copy(text = event.value)
            }
            is AddEditNoteEvent.ChangeTitleFocus -> {
                _noteTitle.value = noteTitle.value.copy(
                    isHintVisible = !event.focusState.isFocused && noteTitle.value.text.isBlank()
                )
            }
            is AddEditNoteEvent.EnteredContent -> {
                val oldText = _noteContent.value.text
                val updatedStyles = adjustStylesForTextChange(oldText, event.value, event.cursorPosition)
                _noteContent.value = _noteContent.value.copy(text = event.value)
                _uiState.value = uiState.value.copy(styles = updatedStyles)
            }
            is AddEditNoteEvent.ChangeContentFocus -> {
                _noteContent.value = _noteContent.value.copy(
                    isHintVisible = !event.focusState.isFocused && _noteContent.value.text.isBlank()
                )
            }
            is AddEditNoteEvent.ChangeColor -> _noteColor.value = event.color
            is AddEditNoteEvent.ChangeFontSize -> _fontSize.value = event.fontSize
            is AddEditNoteEvent.ChangeTextColor -> _textColor.value = event.textColor
            is AddEditNoteEvent.ChangeFontFamily -> _fontFamily.value = event.fontFamily
            is AddEditNoteEvent.ToggleStyle -> {
                val selection = uiState.value.selection
                if (selection.start != selection.end) {
                    toggleStyleInSelection(event.style)
                } else {
                    toggleActiveStyleAtCursor(event.style)
                }
            }
            is AddEditNoteEvent.ApplyColorToSelection -> {
                val selection = uiState.value.selection
                if (selection.start != selection.end) {
                    var currentStyles = uiState.value.styles
                    currentStyles = currentStyles.removeStylesInRange(selection.start, selection.end, Style.COLOR)
                    if (event.color != null) {
                        currentStyles = currentStyles + listOf(
                            StyleRange(
                                start = selection.start,
                                end = selection.end,
                                style = Style.COLOR,
                                color = event.color
                            )
                        )
                    }
                    _uiState.value = uiState.value.copy(styles = currentStyles.optimizeStyles())
                    _activeSelectionColor.value = event.color
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
                    } catch (e: InvalidNoteException) {
                        emmitSnackbar(e.message)
                    }
                }
            }
        }
    }

    private fun adjustStylesForTextChange(
        oldText: String,
        newText: String,
        cursorPos: Int
    ): List<StyleRange> {
        val delta = newText.length - oldText.length
        val currentStyles = uiState.value.styles

        return when {
            delta > 0 -> {
                val insertStart = cursorPos - delta
                val shiftedStyles = currentStyles.adjustForInsertion(insertStart, delta)
                applyPendingStylesToInserted(shiftedStyles, insertStart, cursorPos)
            }
            delta < 0 -> {
                val deleteStart = cursorPos
                val deleteEnd = cursorPos + (-delta)
                currentStyles.adjustForDeletion(deleteStart, deleteEnd)
            }
            else -> currentStyles
        }
    }

    private fun applyPendingStylesToInserted(
        shiftedStyles: List<StyleRange>,
        insertStart: Int,
        insertEnd: Int
    ): List<StyleRange> {
        if (_activeStyles.value.isEmpty()) return shiftedStyles
        val newStyles = shiftedStyles.toMutableList()
        _activeStyles.value.forEach { style ->
            val alreadyCovered = shiftedStyles.any { s ->
                s.style == style && s.start <= insertStart && s.end >= insertEnd
            }
            if (!alreadyCovered) {
                newStyles.add(StyleRange(start = insertStart, end = insertEnd, style = style))
            }
        }
        return newStyles.optimizeStyles()
    }

    private fun toggleStyleInSelection(style: Style) {
        val selection = uiState.value.selection
        if (selection.start == selection.end) return

        val headingStyles = setOf(Style.H1, Style.H2, Style.H3)
        var currentStyles = uiState.value.styles
        val hasStyle = currentStyles.hasStyleInRange(selection.start, selection.end, style)

        if (style in headingStyles) {
            // Headings são mutuamente exclusivos: remove todos antes de aplicar
            for (heading in headingStyles) {
                currentStyles = currentStyles.removeStylesInRange(selection.start, selection.end, heading)
            }
            if (!hasStyle) {
                currentStyles = currentStyles + listOf(
                    StyleRange(start = selection.start, end = selection.end, style = style)
                )
            }
        } else if (hasStyle) {
            currentStyles = currentStyles.removeStylesInRange(selection.start, selection.end, style)
        } else {
            currentStyles = currentStyles + listOf(
                StyleRange(start = selection.start, end = selection.end, style = style)
            )
        }

        _uiState.value = uiState.value.copy(styles = currentStyles.optimizeStyles())
    }

    private fun toggleActiveStyleAtCursor(style: Style) {
        val headingStyles = setOf(Style.H1, Style.H2, Style.H3)
        val current = _activeStyles.value.toMutableSet()

        if (style in headingStyles) {
            val wasActive = style in current
            current.removeAll(headingStyles)
            if (!wasActive) current.add(style)
        } else if (style in current) {
            current.remove(style)
        } else {
            current.add(style)
        }

        _activeStyles.value = current
    }

    fun updateSelection(selection: TextRange) {
        _uiState.value = uiState.value.copy(selection = selection)
        _hasSelection.value = selection.start != selection.end

        val styles = uiState.value.styles

        if (selection.start != selection.end) {
            _activeStyles.value = styles
                .filter { it.start <= selection.start && it.end >= selection.end }
                .map { it.style }
                .toSet()
            _activeSelectionColor.value = styles
                .find { it.style == Style.COLOR && it.start <= selection.start && it.end >= selection.end }
                ?.color
        } else {
            val pos = selection.start
            _activeStyles.value = styles.stylesAtPosition(pos)
            _activeSelectionColor.value = styles.colorAtPosition(pos)
        }
    }

    private fun setDefaultNoteColors() {
        viewModelScope.launch {
            val noteColors = noteUseCases.getNotesColors()
            _defaultColors.emit(Note.noteColors + noteColors.map { Color(it) })
        }
    }

    private suspend fun emmitSnackbar(message: String?) {
        snackbarController.sendEvent(SnackbarEvent(message = message ?: Res.string.couldn_t_save_note))
    }

    fun setColorExpanded(expanded: Boolean) { _colorExpanded.value = expanded }

    fun setSelectedCustomColor(color: Color) { _selectedCustomColor.value = color }

    fun toggleColorDialogVisibility() {
        _isColorDialogVisible.value = !_isColorDialogVisible.value
    }

    fun toggleFontDialogVisibility() {
        _isFontDialogVisible.value = !_isFontDialogVisible.value
    }

    fun toggleSelectionColorDialogVisibility() {
        _isSelectionColorDialogVisible.value = !_isSelectionColorDialogVisible.value
    }
}
