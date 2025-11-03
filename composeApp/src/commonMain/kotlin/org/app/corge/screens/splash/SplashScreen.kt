package org.app.corge.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.splash
import corge.composeapp.generated.resources.splash_kintsugi
import corge.composeapp.generated.resources.splash_wabi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.screens.Screen
import org.app.corge.screens.settings.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    splashViewModel: SplashViewModel = koinInject(),
    themeRepo: ThemeRepository = koinInject()
) {
    val uiState by splashViewModel.uiState.collectAsState()

    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT
    val bgPainter = splashBackgroundFor(appTheme)

    var startAnimation by remember { mutableStateOf(false) }
    var fadeOut by remember { mutableStateOf(false) }

    val animatedAlpha by animateFloatAsState(
        targetValue = when {
            fadeOut -> 0.7f
            startAnimation -> 1f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = if (fadeOut) 600 else 1000,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.9f,
        animationSpec = tween(1600, easing = FastOutSlowInEasing),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1800)
        fadeOut = true
        delay(600)
        if (uiState.isFirstLaunch) onNavigate(Screen.Onboarding.route)
        else onNavigate(Screen.Home.route)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = bgPainter,
            contentDescription = "Splash",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = animatedAlpha
                    scaleX = scale
                    scaleY = scale
                },
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun splashBackgroundFor(theme: AppTheme): Painter = when (theme) {
    AppTheme.LIGHT    -> painterResource(Res.drawable.splash)
    AppTheme.WABI     -> painterResource(Res.drawable.splash_wabi)
    AppTheme.KINTSUGI -> painterResource(Res.drawable.splash_kintsugi)
}

