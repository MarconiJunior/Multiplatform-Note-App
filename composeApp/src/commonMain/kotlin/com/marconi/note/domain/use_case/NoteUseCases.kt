package com.marconi.note.domain.use_case

import com.marconi.note.domain.repository.DeleteNote
import com.marconi.note.domain.use_case.AddNote
import com.marconi.note.domain.use_case.GetNote
import com.marconi.note.domain.use_case.GetNotes

data class NoteUseCases(
    val getNotes: GetNotes,
    val deleteNote: DeleteNote,
    val addNote: AddNote,
    val getNote: GetNote
)