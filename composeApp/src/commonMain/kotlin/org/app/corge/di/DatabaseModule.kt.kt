package org.app.corge.di

import org.app.corge.data.Corge
import org.app.corge.data.DatabaseDriverFactory
import org.koin.dsl.module

val databaseModule = module {
    single { Corge(get<DatabaseDriverFactory>().createDriver()) }
}