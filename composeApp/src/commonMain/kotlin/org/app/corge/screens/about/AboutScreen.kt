package org.app.corge.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.about_corge_bg
import corge.composeapp.generated.resources.about_kintsugi_bg
import corge.composeapp.generated.resources.about_wabi_bg
import corge.composeapp.generated.resources.ic_back
import corge.composeapp.generated.resources.ic_back_kintsugi
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.screens.settings.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    leftPadding: Dp = 16.dp,
    bottomPadding: Dp = 28.dp,
    themeRepo: ThemeRepository = koinInject()
) {
    val uriHandler = LocalUriHandler.current
    val currentId by themeRepo.currentThemeId.collectAsState(null)
    val selectedTheme = AppTheme.entries.firstOrNull { it.id == currentId } ?: AppTheme.LIGHT

    val bgPainter = aboutBackgroundFor(selectedTheme)
    val linkColor = when (selectedTheme) {
        AppTheme.KINTSUGI -> Color(0xFFFF8727)
        else               -> Color(0xFF7A5E45)
    }

    AboutCorgeScreen(
        background = bgPainter,
        onBack = onBack,
        onPrivacy = {
            uriHandler.openUri("https://neostack.top/zyGgKP")
        },
        modifier = modifier,
        leftPadding = leftPadding,
        bottomPadding = bottomPadding,
        linkColor = linkColor
    )
}

@Composable
private fun aboutBackgroundFor(theme: AppTheme): Painter = when (theme) {
    AppTheme.LIGHT    -> painterResource(Res.drawable.about_corge_bg)
    AppTheme.WABI     -> painterResource(Res.drawable.about_wabi_bg)
    AppTheme.KINTSUGI -> painterResource(Res.drawable.about_kintsugi_bg)
}

@Composable
private fun AboutCorgeScreen(
    background: Painter,
    onBack: () -> Unit,
    onPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
    leftPadding: Dp = 16.dp,
    bottomPadding: Dp = 28.dp,
    linkColor: Color = Color(0xFF7A5E45)
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F0E2))
    ) {
        val screenHeight = maxHeight

        val adaptiveBottomPadding = when {
            screenHeight < 650.dp -> bottomPadding + 60.dp
            screenHeight < 800.dp -> bottomPadding + 30.dp
            else -> bottomPadding
        }

        Image(
            painter = background,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        BackFab(
            onBack = onBack,
            iconSize = 42.dp,
            modifier = Modifier
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(start = 12.dp, top = 40.dp)
                .align(Alignment.TopStart)
        )

        PrivacyLink(
            onPrivacy = onPrivacy,
            color = linkColor,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = leftPadding, bottom = adaptiveBottomPadding)
        )
    }
}

@Composable
private fun PrivacyLink(
    onPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF7A5E45)
) {
    val label = "Privacy Policy"

    val annotated = remember(color) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = color, textDecoration = TextDecoration.Underline)) {
                append(label)
            }
            addStringAnnotation(tag = "privacy", annotation = "", start = 0, end = label.length)
        }
    }

    androidx.compose.foundation.text.ClickableText(
        text = annotated,
        style = LocalTextStyle.current.copy(fontSize = 14.sp, lineHeight = 18.sp),
        modifier = modifier
    ) { offset ->
        annotated.getStringAnnotations(start = offset, end = offset).firstOrNull()?.let {
            if (it.tag == "privacy") onPrivacy()
        }
    }
}


@Composable
private fun BackFab(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    themeRepo: ThemeRepository = koinInject()
) {
    val currentId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val theme = AppTheme.entries.firstOrNull { it.id == currentId } ?: AppTheme.LIGHT

    val backIcon = when (theme) {
        AppTheme.KINTSUGI -> painterResource(Res.drawable.ic_back_kintsugi)
        else               -> painterResource(Res.drawable.ic_back)
    }

    Box(
        modifier = modifier
            .size(iconSize + 16.dp)
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = backIcon,
            contentDescription = "Back",
            tint = Color.Unspecified,
            modifier = Modifier.size(iconSize)
        )
    }
}

