package com.marconi.note.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.marconi.note.data.data_source.NoteDatabase
import com.marconi.note.data.data_source.dbFileName
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual fun createRoomDatabase(): NoteDatabase {
    val dbFile = "${fileDirectory()}/$dbFileName"
    return Room.databaseBuilder<NoteDatabase>(
        name = dbFile
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

@OptIn(ExperimentalForeignApi::class)
private fun fileDirectory(): String {
    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return requireNotNull(documentDirectory).path!!
}