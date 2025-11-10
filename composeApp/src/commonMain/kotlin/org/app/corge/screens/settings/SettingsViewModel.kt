package org.app.corge.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.bg_settings_kintsugi
import corge.composeapp.generated.resources.bg_settings_light
import corge.composeapp.generated.resources.bg_settings_wabi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.app.corge.billing.BillingRepository
import org.app.corge.billing.PurchaseResult
import org.app.corge.data.repository.CorgeRepository
import org.app.corge.data.repository.SettingsRepository
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.pdfExporter.BuildExportPayloadUseCase
import org.app.corge.pdfExporter.ExportResult
import org.app.corge.pdfExporter.ExportViewer
import org.app.corge.pdfExporter.PdfExporter
import org.app.corge.screens.search.SearchUiState
import org.app.corge.sound.SoundController
import org.jetbrains.compose.resources.painterResource
import kotlin.time.TimeSource

enum class AppTheme(val id: String, val title: String) {
    LIGHT("light", "Light Minimal"),
    WABI("wabi", "Wabi-Sabi Ink"),
    KINTSUGI("kintsugi", "Kintsugi Night")
}

data class ThemeUi(
    val theme: AppTheme,
    val locked: Boolean,
    val price: String? = null,
    val selected: Boolean = false
)

data class SettingsUi(
    val loading: Boolean = true,
    val themes: List<ThemeUi> = emptyList(),
    val soundOnStart: Boolean = false,
    val showPaywallFor: ThemeUi? = null,
    val showResetAlert: Boolean = false,
    val showResetConfirm: Boolean = false,
    val exporting: Boolean = false,
    val exportLocation: String? = null,
    val exportError: String? = null,
    val exportFileName: String? = null
)

interface SoundPrefs {
    suspend fun get(): Boolean
    suspend fun set(value: Boolean)
}

private const val KEY_SOUND_ON_START = "sound_on_start"

class SoundPrefsImpl(
    private val settings: Settings
) : SoundPrefs {

    override suspend fun get(): Boolean =
        settings.getBoolean(KEY_SOUND_ON_START, true)

    override suspend fun set(value: Boolean) {
        settings.putBoolean(KEY_SOUND_ON_START, value)
    }
}

class SettingsViewModel(
    private val themeRepo: ThemeRepository,
    private val soundPrefs: SoundPrefs? = null,
    private val soundController: SoundController,
    private val loopAssetName: String = "calm_loop.mp3",
    private val buildExport: BuildExportPayloadUseCase,
    private val pdfExporter: PdfExporter,
    private val exportViewer: ExportViewer,
    private val repo: CorgeRepository
) : ViewModel() {

    var ui by mutableStateOf(SettingsUi())
        private set

    fun load() {
        viewModelScope.launch {
            ui = ui.copy(loading = true)

            withContext(Dispatchers.Default) { themeRepo.initializeThemes() }

            val (dbThemes, currentId) = withContext(Dispatchers.Default) {
                val list = themeRepo.getAllThemes()
                val cur  = themeRepo.getCurrentThemeId() ?: AppTheme.LIGHT.id
                list to cur
            }
            val uiThemes = AppTheme.entries.map { app ->
                val row = dbThemes.firstOrNull { it.id == app.id }
                val isPaid = row?.isPaid == true
                val purchased = row?.purchased == true
                ThemeUi(
                    theme = app,
                    locked = isPaid && !purchased,
                    price = if (isPaid) "$1.99" else null,
                    selected = (currentId == app.id)
                )
            }

            val sound = soundPrefs?.get()
            if (sound == true && !soundController.isPlaying) {
                runCatching { soundController.startLoop(loopAssetName) }
            }
            if (sound != true && soundController.isPlaying) {
                runCatching { soundController.stop() }
            }

            ui = ui.copy(
                loading = false,
                themes = uiThemes,
                soundOnStart = (sound == true),
                showPaywallFor = null
            )
        }
    }

    fun selectTheme(t: ThemeUi) {
        if (t.locked) {
            ui = ui.copy(showPaywallFor = t)
        } else {
            applyTheme(t.theme)
        }
    }

    private fun applyTheme(theme: AppTheme) {
        viewModelScope.launch {
            themeRepo.setCurrentTheme(theme.id)
            ui = ui.copy(
                themes = ui.themes.map { it.copy(selected = it.theme == theme) },
                showPaywallFor = null
            )
        }
    }

    fun export(periodDays: Int = 30) {
        viewModelScope.launch {
            ui = ui.copy(exporting = true, exportLocation = null, exportError = null)

            val mark = TimeSource.Monotonic.markNow()

            val res = runCatching {
                val payload = buildExport(periodDays)
                pdfExporter.export(payload)
            }.getOrElse { ExportResult.Error(it.message ?: "Unknown error") }

            val elapsedMs = mark.elapsedNow().inWholeMilliseconds
            val minMs = 2_000L
            if (elapsedMs < minMs) delay(minMs - elapsedMs)

            when (res) {
                is ExportResult.Ok -> {
                    val name = extractFileName(res.location)
                    ui = ui.copy(
                        exporting = false,
                        exportLocation = res.location,
                        exportFileName = name,
                        exportError = null
                    )
                }
                is ExportResult.Error -> {
                    ui = ui.copy(exporting = false, exportError = res.message)
                }
            }
        }
    }

    fun extractFileName(location: String): String =
        location.substringAfterLast('/').ifEmpty { "Stats.pdf" }

    fun openExport() {
        ui.exportLocation?.let { loc ->
            runCatching { exportViewer.view(loc) }
        }
    }

    fun dismissExportDialog() {
        ui = ui.copy(exportLocation = null, exportError = null)
    }

    fun purchaseSelected() {
        val t = ui.showPaywallFor ?: return
        viewModelScope.launch {
            ui = ui.copy(loading = true)

            delay(600)

            withContext(Dispatchers.Default) {
                themeRepo.markThemePurchased(t.theme.id)
                themeRepo.setCurrentTheme(t.theme.id)
            }

            reloadThemesAndClosePaywall()
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            ui = ui.copy(loading = true)
            delay(300)
            reloadThemesAndClosePaywall()
        }
    }

    private suspend fun reloadThemesAndClosePaywall() {
        val dbThemes = withContext(Dispatchers.Default) { themeRepo.getAllThemes() }
        val currentId = withContext(Dispatchers.Default) { themeRepo.getCurrentThemeId() } ?: AppTheme.LIGHT.id

        val uiThemes = AppTheme.entries.map { app ->
            val fromDb = dbThemes.firstOrNull { it.id == app.id }
            val isPaid = fromDb?.isPaid == true
            val purchased = fromDb?.purchased == true
            ThemeUi(
                theme = app,
                locked = isPaid && !purchased,
                price = if (isPaid) "$1.99" else null,
                selected = (currentId == app.id)
            )
        }
        ui = ui.copy(loading = false, themes = uiThemes, showPaywallFor = null)
    }

    fun toggleSound(enabled: Boolean) {
        ui = ui.copy(soundOnStart = enabled)
        viewModelScope.launch {
            soundPrefs?.set(enabled)
            withContext(Dispatchers.Main.immediate) {
                if (enabled) {
                    if (!soundController.isPlaying) runCatching { soundController.startLoop(loopAssetName) }
                } else {
                    if (soundController.isPlaying) runCatching { soundController.stop() }
                }
            }
        }
    }

    fun dismissPaywall() { ui = ui.copy(showPaywallFor = null) }

    fun askReset() { ui = ui.copy(showResetAlert = true) }
    fun cancelReset() { ui = ui.copy(showResetAlert = false, showResetConfirm = false) }
    fun confirmResetFirst() { ui = ui.copy(showResetAlert = false, showResetConfirm = true) }
    fun confirmResetSecond() {
        viewModelScope.launch {
            ui = ui.copy(loading = true)

            withContext(Dispatchers.Default) { repo.resetProgress() }

            val dbThemes = withContext(Dispatchers.Default) { themeRepo.getAllThemes() }
            val currentId = withContext(Dispatchers.Default) { themeRepo.getCurrentThemeId() } ?: AppTheme.LIGHT.id
            val uiThemes = AppTheme.entries.map { app ->
                val fromDb = dbThemes.firstOrNull { it.id == app.id }
                val isPaid = fromDb?.isPaid == true
                val purchased = fromDb?.purchased == true
                ThemeUi(
                    theme = app,
                    locked = isPaid && !purchased,
                    price = if (isPaid) "$1.99" else null,
                    selected = (currentId == app.id)
                )
            }

            ui = ui.copy(
                loading = false,
                showResetConfirm = false,
                showResetAlert = false,
                themes = uiThemes
            )
        }
    }
}

@Composable
fun settingsBackgroundFor(theme: AppTheme) = when (theme) {
    AppTheme.LIGHT    -> painterResource(Res.drawable.bg_settings_light)
    AppTheme.WABI     -> painterResource(Res.drawable.bg_settings_wabi)
    AppTheme.KINTSUGI -> painterResource(Res.drawable.bg_settings_kintsugi)
}


//class SettingsViewModel(
//    private val themeRepo: ThemeRepository,
//    private val billing: BillingRepository,
//    private val soundPrefs: SoundPrefs? = null,
//    private val soundController: SoundController,
//    private val loopAssetName: String = "calm_loop.mp3",
//    private val buildExport: BuildExportPayloadUseCase,
//    private val pdfExporter: PdfExporter,
//    private val exportViewer: ExportViewer,
//    private val repo: CorgeRepository,
//    private val settings: SettingsRepository
//) : ViewModel() {
//
//    var ui by mutableStateOf(SettingsUi())
//        private set
//
//    fun load() {
//        viewModelScope.launch {
//            ui = ui.copy(loading = true)
//
//            withContext(Dispatchers.Default) { themeRepo.initializeThemes() }
//
//            val (dbThemes, currentId) = withContext(Dispatchers.Default) {
//                val list = themeRepo.getAllThemes()
//                val cur  = themeRepo.getCurrentThemeId() ?: AppTheme.LIGHT.id
//                list to cur
//            }
//            val uiThemes = AppTheme.entries.map { app ->
//                val row = dbThemes.firstOrNull { it.id == app.id }
//                val isPaid = row?.isPaid == true
//                val purchased = row?.purchased == true
//                ThemeUi(
//                    theme = app,
//                    locked = isPaid && !purchased,
//                    price = if (isPaid) "$1.99" else null,
//                    selected = (currentId == app.id)
//                )
//            }
//
//            val sound = soundPrefs?.get()
//            if (sound == true && !soundController.isPlaying) {
//                runCatching { soundController.startLoop(loopAssetName) }
//            }
//            if (sound != true && soundController.isPlaying) {
//                runCatching { soundController.stop() }
//            }
//
//            ui = ui.copy(
//                loading = false,
//                themes = uiThemes,
//                soundOnStart = (sound == true),
//                showPaywallFor = null
//            )
//        }
//    }
//
//    fun selectTheme(t: ThemeUi) {
//        if (t.locked) {
//            ui = ui.copy(showPaywallFor = t)
//        } else {
//            applyTheme(t.theme)
//        }
//    }
//
//    private fun applyTheme(theme: AppTheme) {
//        viewModelScope.launch {
//            themeRepo.setCurrentTheme(theme.id)
//            ui = ui.copy(
//                themes = ui.themes.map { it.copy(selected = it.theme == theme) },
//                showPaywallFor = null
//            )
//        }
//    }
//
//    fun export(periodDays: Int = 30) {
//        viewModelScope.launch {
//            ui = ui.copy(exporting = true, exportLocation = null, exportError = null)
//
//            val mark = TimeSource.Monotonic.markNow()
//
//            val res = runCatching {
//                val payload = buildExport(periodDays)
//                pdfExporter.export(payload)
//            }.getOrElse { ExportResult.Error(it.message ?: "Unknown error") }
//
//            val elapsedMs = mark.elapsedNow().inWholeMilliseconds
//            val minMs = 2_000L
//            if (elapsedMs < minMs) delay(minMs - elapsedMs)
//
//            when (res) {
//                is ExportResult.Ok -> {
//                    val name = extractFileName(res.location)
//                    ui = ui.copy(
//                        exporting = false,
//                        exportLocation = res.location,
//                        exportFileName = name,
//                        exportError = null
//                    )
//                }
//                is ExportResult.Error -> {
//                    ui = ui.copy(exporting = false, exportError = res.message)
//                }
//            }
//        }
//    }
//
//    fun extractFileName(location: String): String =
//        location.substringAfterLast('/').ifEmpty { "Stats.pdf" }
//
//    fun openExport() {
//        ui.exportLocation?.let { loc ->
//            runCatching { exportViewer.view(loc) }
//        }
//    }
//    fun dismissExportDialog() {
//        ui = ui.copy(exportLocation = null, exportError = null)
//    }
//
//    fun purchaseSelected() {
//        val t = ui.showPaywallFor ?: return
//        viewModelScope.launch {
//            ui = ui.copy(loading = true)
//            when (val res = billing.purchaseTheme(t.theme.id)) {
//                is PurchaseResult.Success -> {
//                    reloadAfterPurchase()
//                }
//                is PurchaseResult.Failure -> {
//                    ui = ui.copy(loading = false, showPaywallFor = null)
//                }
//                is PurchaseResult.Error -> {
//                    ui = ui.copy(loading = false, showPaywallFor = null)
//                }
//            }
//        }
//    }
//
//    fun restorePurchases() {
//        viewModelScope.launch {
//            ui = ui.copy(loading = true)
//            when (billing.restorePurchases()) {
//                is PurchaseResult.Success,
//                is PurchaseResult.Failure,
//                is PurchaseResult.Error -> {
//                    reloadAfterPurchase()
//                }
//            }
//        }
//    }
//
//    private suspend fun reloadAfterPurchase() {
//        val dbThemes = themeRepo.getAllThemes()
//        val currentId = themeRepo.getCurrentThemeId() ?: AppTheme.LIGHT.id
//        val uiThemes = AppTheme.entries.map { app ->
//            val fromDb = dbThemes.firstOrNull { it.id == app.id }
//            val isPaid = fromDb?.isPaid == true
//            val purchased = fromDb?.purchased == true
//            ThemeUi(
//                theme = app,
//                locked = isPaid && !purchased,
//                price = if (isPaid) "$1.99" else null,
//                selected = (currentId == app.id)
//            )
//        }
//        ui = ui.copy(loading = false, themes = uiThemes, showPaywallFor = null)
//    }
//
//    fun toggleSound(enabled: Boolean) {
//        ui = ui.copy(soundOnStart = enabled)
//
//        viewModelScope.launch {
//            soundPrefs?.set(enabled)
//
//            withContext(Dispatchers.Main.immediate) {
//                if (enabled) {
//                    if (!soundController.isPlaying) {
//                        runCatching { soundController.startLoop(loopAssetName) }
//                    }
//                } else {
//                    if (soundController.isPlaying) {
//                        runCatching { soundController.stop() }
//                    }
//                }
//            }
//        }
//    }
//
//    fun dismissPaywall() { ui = ui.copy(showPaywallFor = null) }
//    fun askReset() { ui = ui.copy(showResetAlert = true) }
//    fun cancelReset() { ui = ui.copy(showResetAlert = false, showResetConfirm = false) }
//    fun confirmResetFirst() { ui = ui.copy(showResetAlert = false, showResetConfirm = true) }
//    fun confirmResetSecond() {
//        viewModelScope.launch {
//            ui = ui.copy(loading = true)
//
//            repo.resetProgress()
//            settings.clearRecentSearches()
//
//            val dbThemes = themeRepo.getAllThemes()
//            val currentId = themeRepo.getCurrentThemeId() ?: AppTheme.LIGHT.id
//            val uiThemes = AppTheme.entries.map { app ->
//                val fromDb = dbThemes.firstOrNull { it.id == app.id }
//                val isPaid = fromDb?.isPaid == true
//                val purchased = fromDb?.purchased == true
//                ThemeUi(
//                    theme = app,
//                    locked = isPaid && !purchased,
//                    price = if (isPaid) "$1.99" else null,
//                    selected = (currentId == app.id)
//                )
//            }
//
//            ui = ui.copy(
//                loading = false,
//                showResetConfirm = false,
//                showResetAlert = false,
//                themes = uiThemes
//            )
//        }
//    }
//}