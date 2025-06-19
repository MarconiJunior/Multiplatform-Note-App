package com.marconi.kipi.di

import com.marconi.kipi.data.data_source.NoteDatabase
import com.marconi.kipi.data.data_source.getNoteDatabase
import com.marconi.kipi.data.repository.NoteRepositoryImpl
import com.marconi.kipi.domain.repository.DeleteNote
import com.marconi.kipi.domain.repository.NoteRepository
import com.marconi.kipi.domain.use_case.AddNote
import com.marconi.kipi.domain.use_case.GetNote
import com.marconi.kipi.domain.use_case.GetNotes
import com.marconi.kipi.domain.use_case.NoteUseCases
import com.marconi.kipi.events.CommonEvents
import com.marconi.kipi.presentation.add_edit_note.AddEditNoteViewModel
import com.marconi.kipi.presentation.main.MainViewModel
import com.marconi.kipi.presentation.notes.NotesViewModel
import com.marconi.kipi.presentation.util.ThemeManager
import com.marconi.kipi.snackbar_utils.SnackbarController
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val targetModule: Module

val appModule = module {
    single { getNoteDatabase(get()) }

    single { get<NoteDatabase>().noteDao() }

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

    single { ThemeManager(get()) }

    single<CommonEvents> {
        CommonEvents()
    }

    single {
        SnackbarController()
    }

    viewModelOf(::NotesViewModel)
    viewModelOf(::AddEditNoteViewModel)
    viewModelOf(::MainViewModel)
}

fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null
) {
    startKoin {
        config?.invoke(this)
        modules(targetModule, appModule)
    }
}
