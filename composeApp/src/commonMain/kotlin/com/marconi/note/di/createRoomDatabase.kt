package com.marconi.note.di

import com.marconi.note.data.data_source.NoteDatabase

expect fun createRoomDatabase(): NoteDatabase
