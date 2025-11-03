package org.app.corge.di

import org.app.corge.billing.AndroidBillingRepository
import org.app.corge.billing.BillingRepository
import org.app.corge.data.AndroidDatabaseDriverFactory
import org.app.corge.data.DatabaseDriverFactory
import org.app.corge.pdfExporter.AndroidExportViewer
import org.app.corge.pdfExporter.ExportViewer
import org.app.corge.pdfExporter.PdfExporter
import org.app.corge.sound.SoundController
import org.app.corge.sound.TtsController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(androidContext()) }
    single { SoundController() }
    single { TtsController() }
    single<BillingRepository> { AndroidBillingRepository(androidContext(), get()) }
    single { PdfExporter() }
    single<ExportViewer> { AndroidExportViewer(androidContext()) }
}