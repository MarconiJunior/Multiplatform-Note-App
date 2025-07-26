package com.marconi.kipi.data.data_source

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

fun getNoteDatabase(
builder: RoomDatabase.Builder<NoteDatabase>
): NoteDatabase {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(NoteDatabase.MIGRATION_1_2)
        .build()
}