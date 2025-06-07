package com.marconi.note.presentation.notes

import com.marconi.note.domain.model.Note
import com.marconi.note.domain.util.NoteOrder
import com.marconi.note.domain.util.OrderType

data class NotesState(
    val notes: List<Note> = emptyList(),
    val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false
)