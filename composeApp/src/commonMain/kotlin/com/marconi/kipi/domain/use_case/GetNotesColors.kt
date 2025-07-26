package com.marconi.kipi.domain.use_case

import com.marconi.kipi.domain.repository.NoteRepository

class GetNotesColors(
private val repository: NoteRepository
) {
    suspend operator fun invoke(): List<Int> {
        return repository.getNotesColors()
    }
}