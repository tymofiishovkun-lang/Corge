package org.app.corge.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.app.corge.data.repository.RemoteConfigRepository
import org.app.corge.data.repository.SettingsRepository

class SplashViewModel(
    private val settings: SettingsRepository,
    private val remoteConfig: RemoteConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        viewModelScope.launch {
            println("🟦 [Splash] ViewModel init → starting startup logic")
            runStartupLogic()
        }
    }

    private suspend fun runStartupLogic() {

        val saved = remoteConfig.getSavedUrl()
        println("🟣 [Splash] Step 1: savedUrl = $saved")

        if (!saved.isNullOrBlank()) {
            println("🟢 [Splash] Saved URL FOUND → show web → $saved")
            _uiState.value = SplashUiState.ShowWeb(saved)
            return
        }

        val isFirst = settings.isFirstLaunch()
        println("🔵 [Splash] Step 2: isFirstLaunch = $isFirst")

        if (isFirst) {

            println("🔵 [RemoteConfig] Sending POST → fetchUrl()")
            val result = remoteConfig.fetchUrl()
            println("🟣 [Splash] fetchedUrl = $result")

            if (!result.isNullOrBlank() && result.startsWith("http")) {
                println("🟡 [RemoteConfig] Saving URL → $result")
                remoteConfig.saveUrl(result)

                println("🟡 [Splash] Setting firstLaunch = false")
                settings.setFirstLaunch(false)

                println("🟢 [Splash] Loaded first URL → $result")
                _uiState.value = SplashUiState.ShowWeb(result)
                return
            }

            println("🔴 [Splash] invalid URL → ShowApp")
            _uiState.value = SplashUiState.ShowApp
            return
        }

        println("🟧 [Splash] Step 6: not first launch → ShowApp")
        _uiState.value = SplashUiState.ShowApp
    }

    fun markLaunched() {
        viewModelScope.launch {
            println("🟡 [Splash] markLaunched()")
            settings.setFirstLaunch(false)
            _uiState.value = SplashUiState.ShowApp
        }
    }

    suspend fun isFirstLaunch(): Boolean {
        val res = settings.isFirstLaunch()
        println("🟣 [Splash] isFirstLaunch() → $res")
        return res
    }
}

sealed class SplashUiState {
    object Loading : SplashUiState()
    object ShowApp : SplashUiState()
    data class ShowWeb(val url: String) : SplashUiState()
}


