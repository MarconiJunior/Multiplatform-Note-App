package com.marconi.kipi.domain.use_case

import com.marconi.kipi.domain.repository.DeleteNote

data class NoteUseCases(
    val getNotes: GetNotes,
    val deleteNote: DeleteNote,
    val addNote: AddNote,
    val getNote: GetNote,
    val getNotesColors: GetNotesColors
)