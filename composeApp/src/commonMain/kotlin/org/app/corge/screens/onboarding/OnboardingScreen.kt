package org.app.corge.screens.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.btn_get_started
import corge.composeapp.generated.resources.btn_skip
import corge.composeapp.generated.resources.onboarding_slide1
import corge.composeapp.generated.resources.onboarding_slide2
import corge.composeapp.generated.resources.onboarding_slide3
import kotlinx.coroutines.launch
import org.app.corge.screens.Screen
import org.app.corge.screens.splash.SplashViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigate: (String) -> Unit,
    splashViewModel: SplashViewModel = koinInject()
) {
    val pages = listOf(
        Res.drawable.onboarding_slide1,
        Res.drawable.onboarding_slide2,
        Res.drawable.onboarding_slide3
    )

    val pagerState = rememberPagerState(initialPage = 0) { pages.size }
    val scope = rememberCoroutineScope()

    var startAnimation by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "fadeInAlpha"
    )

    val translateY by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 60.dp,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "slideUp"
    )

    LaunchedEffect(Unit) { startAnimation = true }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = translateY.toPx()
            }
    ) {
        val screenHeight = maxHeight

        val buttonWidth = when {
            screenHeight < 650.dp -> 250.dp
            screenHeight < 800.dp -> 300.dp
            else -> 350.dp
        }
        val buttonHeight = when {
            screenHeight < 650.dp -> 56.dp
            screenHeight < 800.dp -> 70.dp
            else -> 80.dp
        }

        val bottomPadding = when {
            screenHeight < 650.dp -> 60.dp
            screenHeight < 800.dp -> 140.dp
            else -> 220.dp
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().clipToBounds()
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val scale = 1f - 0.1f * kotlin.math.abs(pageOffset)
            val pageAlpha = 1f - 0.5f * kotlin.math.abs(pageOffset)

            Image(
                painter = painterResource(pages[page]),
                contentDescription = "Onboarding Slide ${page + 1}",
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.1f)
                    .graphicsLayer {
                        this.scaleX = scale
                        this.scaleY = scale
                        this.alpha = pageAlpha
                    },
                contentScale = ContentScale.Crop
            )
        }

        Image(
            painter = painterResource(Res.drawable.btn_skip),
            contentDescription = "Skip",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = screenHeight * 0.08f, end = 8.dp)
                .size(width = 110.dp, height = 44.dp)
                .clickable {
                    splashViewModel.markLaunched()
                    onNavigate(Screen.Home.route)
                }
        )

        val isLastPage = pagerState.currentPage == pages.lastIndex

        val buttonAlpha by animateFloatAsState(
            targetValue = if (isLastPage) 1f else 0.9f,
            animationSpec = tween(500),
            label = "buttonAlpha"
        )

        Image(
            painter = painterResource(Res.drawable.btn_get_started),
            contentDescription = if (isLastPage) "Get Started" else "Continue",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomPadding)
                .size(width = buttonWidth, height = buttonHeight)
                .graphicsLayer {
                    this.alpha = buttonAlpha
                    this.scaleX = if (isLastPage) 1.05f else 1f
                    this.scaleY = if (isLastPage) 1.05f else 1f
                }
                .clickable {
                    if (isLastPage) {
                        splashViewModel.markLaunched()
                        onNavigate(Screen.Home.route)
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1,
                                animationSpec = tween(
                                    durationMillis = 600,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                    }
                }
        )
    }
}