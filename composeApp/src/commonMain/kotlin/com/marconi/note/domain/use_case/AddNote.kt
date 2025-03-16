package com.marconi.note.domain.use_case

import com.marconi.note.domain.model.InvalidNoteException
import com.marconi.note.domain.model.Note
import com.marconi.note.domain.repository.NoteRepository
import kotlin.coroutines.cancellation.CancellationException

class AddNote(
    private val repository: NoteRepository
) {
    @Throws(InvalidNoteException::class, CancellationException::class)
    suspend operator fun invoke(note: Note) {
        if (note.title.isBlank()) {
            throw InvalidNoteException("The title of the note can't be empty")
        }
        if (note.content.isBlank()) {
            throw InvalidNoteException("The content of the note can't be empty")
        }
        repository.insertNote(note)
    }
}