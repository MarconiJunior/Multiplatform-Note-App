package com.marconi.note.di

import androidx.room.Room
import com.marconi.note.data.data_source.NoteDatabase
import com.marconi.note.data.repository.NoteRepositoryImpl
import com.marconi.note.domain.repository.DeleteNote
import com.marconi.note.domain.repository.NoteRepository
import com.marconi.note.domain.use_case.AddNote
import com.marconi.note.domain.use_case.GetNote
import com.marconi.note.domain.use_case.GetNotes
import com.marconi.note.domain.use_case.NoteUseCases
import com.marconi.note.events.CommonEvents
import com.marconi.note.presentation.util.ThemeManager
import com.marconi.note.snackbar_utils.SnackbarController
import org.koin.dsl.module

val appModule = module {
    single<NoteDatabase> {
        createRoomDatabase()
    }

    single<NoteRepository> {
        NoteRepositoryImpl(get())
    }

    single<NoteUseCases> {
        NoteUseCases(
            getNotes = GetNotes(get()),
            deleteNote = DeleteNote(get()),
            addNote = AddNote(get()),
            getNote = GetNote(get())
        )
    }

    single<CommonEvents> {
        CommonEvents()
    }

    single {
        ThemeManager()
    }

    single {
        SnackbarController()
    }
}