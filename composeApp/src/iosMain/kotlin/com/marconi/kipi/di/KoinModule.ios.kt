package com.marconi.kipi.di

import com.marconi.kipi.data.createDataStore
import com.marconi.kipi.data.data_source.getDatabaseBuilder
import org.koin.dsl.module

actual val targetModule = module {
    single { getDatabaseBuilder() }
    single { createDataStore() }
}