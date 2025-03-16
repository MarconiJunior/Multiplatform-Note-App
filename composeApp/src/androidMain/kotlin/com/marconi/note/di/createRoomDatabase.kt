package com.marconi.note.di

import android.app.Application
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.marconi.note.data.data_source.NoteDatabase
import com.marconi.note.data.data_sourcee.dbFileName
import com.example.fruitties.network.FruittieApi
import kotlinx.coroutines.Dispatchers

actual fun createRoomDatabase(app: Application): NoteDatabase {
    val dbFile = app.getDatabasePath(dbFileName)
    return Room.databaseBuilder<NoteDatabase>(
        context = app,
        name = dbFile.absolutePath
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}