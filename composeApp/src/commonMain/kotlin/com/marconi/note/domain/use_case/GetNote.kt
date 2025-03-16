package com.marconi.note.domain.use_case

import com.marconi.note.domain.model.Note
import com.marconi.note.domain.repository.NoteRepository

class GetNote(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(id: Int): Note? {
        return repository.getNoteById(id)
    }
}