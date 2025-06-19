package com.marconi.kipi.presentation.add_edit_note

import androidx.compose.ui.focus.FocusState

sealed class AddEditNoteEvent {
    data class EnteredTitle(val value: String): AddEditNoteEvent()
    data class ChangeTitleFocus(val focusState: FocusState): AddEditNoteEvent()
    data class EnteredContent(val value: String): AddEditNoteEvent()
    data class ChangeContentFocus(val focusState: FocusState): AddEditNoteEvent()
    data class ChangeColor(val color: Int): AddEditNoteEvent()
    data class ChangeFontSize(val fontSize: Float): AddEditNoteEvent()
    data class ChangeTextColor(val textColor: Int): AddEditNoteEvent()
    data object SaveNote: AddEditNoteEvent()
}
