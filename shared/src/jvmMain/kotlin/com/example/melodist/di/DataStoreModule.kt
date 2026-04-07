package com.example.melodist.di

import com.example.melodist.data.local.createDataStore
import com.example.melodist.data.repository.UserPreferencesRepository
import org.koin.dsl.module

val dataStoreModule = module {
    single { createDataStore() }
    single { UserPreferencesRepository(get()) }
}