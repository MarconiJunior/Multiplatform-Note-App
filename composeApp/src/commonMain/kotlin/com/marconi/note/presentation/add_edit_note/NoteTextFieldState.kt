package com.marconi.note.presentation.add_edit_note

import org.jetbrains.compose.resources.StringResource

data class NoteTextFieldState(
    val text: String = "",
    val hint: StringResource? = null,
    val isHintVisible: Boolean = true
)
