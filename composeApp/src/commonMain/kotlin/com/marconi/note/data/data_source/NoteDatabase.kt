package com.marconi.note.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.marconi.note.domain.model.Note

@Database(
    entities = [Note::class],
    version = 1,
)
abstract class NoteDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        const val DATABASE_NAME = "notes_db"
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object NoteDatabaseConstructor : RoomDatabaseConstructor<NoteDatabase> {
    override fun initialize(): NoteDatabase
}
internal const val dbFileName = "notes.db"
