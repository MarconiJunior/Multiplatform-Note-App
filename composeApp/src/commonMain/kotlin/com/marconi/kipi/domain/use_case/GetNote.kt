package com.marconi.kipi.domain.use_case

import com.marconi.kipi.domain.model.Note
import com.marconi.kipi.domain.repository.NoteRepository

class GetNote(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(id: Int): Note? {
        return repository.getNoteById(id)
    }
}