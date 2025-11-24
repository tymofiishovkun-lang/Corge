package org.app.corge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.compose_multiplatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.app.corge.data.repository.SettingsRepository
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.screens.LoadingScreen
import org.app.corge.screens.Screen
import org.app.corge.screens.about.AboutScreen
import org.app.corge.screens.detail.DetailsScreen
import org.app.corge.screens.favorite.FavoritesScreen
import org.app.corge.screens.home.HomeScreen
import org.app.corge.screens.journal.JournalScreen
import org.app.corge.screens.onboarding.OnboardingScreen
import org.app.corge.screens.search.SearchResultsScreen
import org.app.corge.screens.search.SearchScreen
import org.app.corge.screens.search.SearchViewModel
import org.app.corge.screens.session.SessionScreen
import org.app.corge.screens.settings.SettingsScreen
import org.app.corge.screens.settings.SoundPrefs
import org.app.corge.screens.splash.SplashScreen
import org.app.corge.screens.splash.SplashUiState
import org.app.corge.screens.splash.SplashViewModel
import org.app.corge.screens.stats.StatsScreen
import org.app.corge.sound.SoundController
import org.koin.compose.getKoin
import org.koin.compose.koinInject

@Composable
fun App() {
    val nav = remember { SimpleNavigator(Screen.Splash.route) }

    val soundPrefs: SoundPrefs = koinInject()
    val soundController: SoundController = koinInject()
    val themeRepo: ThemeRepository = koinInject()
    val splashViewModel: SplashViewModel = koinInject()
    val uiState by splashViewModel.uiState.collectAsState()

    val loopAssetName = "calm_loop.mp3"

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            themeRepo.initializeThemes()
        }
    }

    when (val state = uiState) {

        SplashUiState.Loading -> {
            LoadingScreen()
        }

        is SplashUiState.ShowWeb -> {

            LaunchedEffect("enter_web") {
                if (soundController.isPlaying) {
                    runCatching { soundController.stop() }
                }
            }

            WebScreen(url = state.url)
        }

        SplashUiState.ShowApp -> {

            LaunchedEffect("enter_app") {
                val enabled = soundPrefs.get()
                if (enabled && !soundController.isPlaying) {
                    runCatching { soundController.startLoop(loopAssetName) }
                }
            }

            AnimatedNavHostCrossfade(
                navigator = nav,
                searchVm = koinInject()
            )
        }
    }
}

@Composable
fun AnimatedNavHostCrossfade(
    navigator: SimpleNavigator,
    searchVm: SearchViewModel
) {
    Crossfade(
        targetState = navigator.current,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
    ) { route ->
        when {
            route == Screen.Splash.route -> SplashScreen(navigator::navigate)

            route == Screen.Onboarding.route -> OnboardingScreen(navigator::navigate)

            route == Screen.Home.route -> HomeScreen(
                onOpenExplore = { navigator.navigate(Screen.Search.route) },
                onOpenSettings = { navigator.navigate(Screen.Setting.route) },
                onOpenMessageDetails = { id -> navigator.navigate(Screen.Details.route(id)) },
                onOpenFavorites = { navigator.navigate(Screen.Favorites.route) },
                onOpenJournal = { navigator.navigate(Screen.Journal.route) },
                onOpenStats = { navigator.navigate(Screen.Stats.route) },
                onOpenCategory = { categoryId ->
                    searchVm.selectCategoryAndSearch(categoryId)
                    searchVm.applyFilters()
                    navigator.navigate(Screen.SearchResults.route)
                },
                viewModel = koinInject(),
                themeRepo = koinInject(),
                settingsRepository = koinInject()
            )

            route == Screen.Search.route -> SearchScreen(
                viewModel = searchVm,
                onBack = navigator::back,
                onOpenDetails  = { id -> navigator.navigate(Screen.Details.route(id)) },
                onOpenResults  = { navigator.navigate(Screen.SearchResults.route) },
                onOpenHome = { navigator.navigate(Screen.Home.route) },
                onOpenSettings = { navigator.navigate(Screen.Setting.route) }
            )

            route == Screen.SearchResults.route -> SearchResultsScreen(
                vm = searchVm,
                onBack = {
                    searchVm.toFilters()
                    navigator.back()
                },
                onOpenDetails = { id, date ->
                    navigator.navigate(Screen.Details.route(id, date))
                }
            )

            route == Screen.Journal.route -> JournalScreen(
                onBack = navigator::back,
                onOpenDetails = { id, date ->
                    navigator.navigate(Screen.Details.route(id, date))
                }
            )

            route == Screen.Favorites.route -> FavoritesScreen(
                onBack = navigator::back,
                onOpenDetails = { id, date ->
                    navigator.navigate(Screen.Details.route(id, date))
                }
            )

            route == Screen.Stats.route -> StatsScreen(onBack = navigator::back)
            route == Screen.Setting.route -> SettingsScreen(
                onBack = navigator::back,
                onOpenAbout = { navigator.navigate(Screen.About.route) },
                onOpenHome = {navigator.navigate(Screen.Home.route)},
                onOpenExplore = {navigator.navigate(Screen.Search.route)}
            )

            route == Screen.About.route -> AboutScreen(
                onBack = navigator::back
            )

            Screen.Details.isMatch(route) -> {
                val parsed = Screen.Details.parse(route) ?: return@Crossfade
                val (id, date) = parsed
                DetailsScreen(
                    messageId      = id,
                    dateArg        = date,
                    onBack         = navigator::back,
                    onStartSession = { mid -> navigator.navigate(Screen.Session.route(mid)) },
                    platformHandler = getPlatformHandler()
                )
            }

            Screen.Session.isMatch(route) -> {
                val id = Screen.Session.idFrom(route) ?: return@Crossfade
                SessionScreen(
                    messageId = id,
                    onBack = navigator::back,
                    onFinished = { navigator.back() }
                )
            }
        }
    }
}

@Stable
class SimpleNavigator(start: String) {
    private val _stack = mutableStateListOf(start)
    val current: String get() = _stack.last()

    fun navigate(route: String) { _stack += route }
    fun back() { if (_stack.size > 1) _stack.removeLast() }
}
