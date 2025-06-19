package com.marconi.kipi.presentation.notes

import com.marconi.kipi.domain.model.Note
import com.marconi.kipi.domain.util.NoteOrder
import com.marconi.kipi.domain.util.OrderType

data class NotesState(
    val notes: List<Note> = emptyList(),
    val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false
)