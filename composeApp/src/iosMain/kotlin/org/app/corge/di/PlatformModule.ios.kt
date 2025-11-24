package org.app.corge.di

import org.app.corge.billing.BillingRepository
import org.app.corge.billing.IOSBillingRepository
import org.app.corge.data.DatabaseDriverFactory
import org.app.corge.data.IOSDatabaseDriverFactory
import org.app.corge.network.NetworkViewModel
import org.app.corge.pdfExporter.ExportViewer
import org.app.corge.pdfExporter.IOSExportViewer
import org.app.corge.pdfExporter.PdfExporter
import org.app.corge.sound.SoundController
import org.app.corge.sound.TtsController
import org.koin.dsl.module

actual val platformModule = module {
    single<DatabaseDriverFactory> { IOSDatabaseDriverFactory() }
    single { SoundController() }
    single { TtsController() }
    single<BillingRepository> { IOSBillingRepository(get()) }
    single { PdfExporter() }
    single<ExportViewer> { IOSExportViewer() }
    single { NetworkViewModel() }
}

object KoinStarter {
    fun start() = initKoin()
}