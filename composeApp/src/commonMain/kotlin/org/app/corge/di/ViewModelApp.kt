package org.app.corge.di

import com.russhwolf.settings.Settings
import org.app.corge.screens.detail.DetailsViewModel
import org.app.corge.screens.favorite.FavoritesViewModel
import org.app.corge.screens.home.HomeViewModel
import org.app.corge.screens.journal.JournalViewModel
import org.app.corge.screens.search.SearchViewModel
import org.app.corge.screens.session.SessionViewModel
import org.app.corge.screens.settings.SettingsViewModel
import org.app.corge.screens.splash.SplashViewModel
import org.app.corge.screens.stats.StatsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    single { Settings() }
    viewModelOf(::SplashViewModel)
    single { HomeViewModel(get(), get(), get()) }
    single { SearchViewModel(get(), get()) }
    single { DetailsViewModel(get(), get()) }
    single { SessionViewModel(get(), get()) }
    single { FavoritesViewModel(get()) }
    single { JournalViewModel(get()) }
    single { StatsViewModel(get()) }
    single {
        SettingsViewModel(
            themeRepo = get(),
            billing = get(),
            soundPrefs = getOrNull(),
            soundController = get(),
            buildExport = get(),
            pdfExporter = get(),
            exportViewer = get(),
            repo = get(),
            settings = get()
        )
    }
}