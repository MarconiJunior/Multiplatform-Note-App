package com.marconi.note.domain.repository

import com.marconi.note.domain.model.Note

class DeleteNote(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.deleteNote(note)
    }
}