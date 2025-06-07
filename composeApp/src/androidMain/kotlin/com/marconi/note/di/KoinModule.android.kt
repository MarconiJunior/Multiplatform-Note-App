package com.marconi.note.di

import com.marconi.note.data.createDataStore
import com.marconi.note.data.data_source.getDatabaseBuilder
import org.koin.dsl.module

actual val targetModule = module {
    single { getDatabaseBuilder(context = get()) }
    single { createDataStore(context = get()) }
}