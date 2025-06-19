package com.marconi.kipi.domain.repository

import com.marconi.kipi.domain.model.Note

class DeleteNote(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.deleteNote(note)
    }
}