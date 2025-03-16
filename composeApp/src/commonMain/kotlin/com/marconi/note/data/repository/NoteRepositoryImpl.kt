package com.marconi.note.data.repository

import com.marconi.note.data.data_source.NoteDao
import com.marconi.note.domain.model.Note
import com.marconi.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

class NoteRepositoryImpl(
    private val dao: NoteDao,
) : NoteRepository {
    override fun getNotes(): Flow<List<Note>> {
        return dao.getNotes()
    }

    override suspend fun getNoteById(id: Int): Note? {
        return dao.getNoteById(id)
    }

    override suspend fun insertNote(note: Note) {
        dao.insertNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        dao.deleteNote(note)
    }
}
