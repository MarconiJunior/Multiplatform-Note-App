package com.marconi.note.presentation.add_edit_note

data class NoteTextFieldState(
    val text: String = "",
    val hint: Int? = null,
    val isHintVisible: Boolean = true
)
