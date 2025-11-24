package org.app.corge.di

import org.app.corge.data.repository.CorgeRepository
import org.app.corge.data.repository.CorgeRepositoryImpl
import org.app.corge.data.repository.RemoteConfigRepository
import org.app.corge.data.repository.RemoteConfigRepositoryImpl
import org.app.corge.data.repository.SettingsRepository
import org.app.corge.data.repository.SettingsRepositoryImpl
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.data.repository.ThemeRepositoryImpl
import org.app.corge.pdfExporter.BuildExportPayloadUseCase
import org.app.corge.screens.settings.SoundPrefs
import org.app.corge.screens.settings.SoundPrefsImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<CorgeRepository> { CorgeRepositoryImpl(get()) }
    single<ThemeRepository> { ThemeRepositoryImpl(get()) }
    single<SoundPrefs> { SoundPrefsImpl(get()) }
    single { BuildExportPayloadUseCase(get()) }
    single<RemoteConfigRepository> { RemoteConfigRepositoryImpl(get()) }
}