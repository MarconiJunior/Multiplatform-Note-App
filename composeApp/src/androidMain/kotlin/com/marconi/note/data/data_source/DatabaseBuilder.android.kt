package com.marconi.note.data.data_source

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<NoteDatabase> {
    val dbFile = context.getDatabasePath("note.db")
    return Room.databaseBuilder(
        context = context,
        name = dbFile.absolutePath
    )
}