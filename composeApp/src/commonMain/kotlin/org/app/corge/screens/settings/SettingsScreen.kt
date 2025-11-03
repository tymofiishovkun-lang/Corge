package org.app.corge.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.ic_export
import corge.composeapp.generated.resources.ic_paywall_bag
import corge.composeapp.generated.resources.ic_paywall_clock
import corge.composeapp.generated.resources.ic_paywall_lock
import corge.composeapp.generated.resources.ic_reset
import corge.composeapp.generated.resources.ic_sound_off
import corge.composeapp.generated.resources.ic_sound_on
import corge.composeapp.generated.resources.illustration_reset_dog
import corge.composeapp.generated.resources.theme_kintsugi_locked
import corge.composeapp.generated.resources.theme_kintsugi_unlocked
import corge.composeapp.generated.resources.theme_kintsugi_unselected
import corge.composeapp.generated.resources.theme_light_selected
import corge.composeapp.generated.resources.theme_light_unselected
import corge.composeapp.generated.resources.theme_wabi_locked
import corge.composeapp.generated.resources.theme_wabi_unlocked
import corge.composeapp.generated.resources.theme_wabi_unselected
import org.app.corge.pdfExporter.exportFolderHint
import org.app.corge.screens.bottomBar.BottomBar
import org.app.corge.screens.home.HomePalette
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenExplore: () -> Unit = {},
    onOpenHome: () -> Unit,
    vm: SettingsViewModel = koinInject()
) {
    LaunchedEffect(Unit) { vm.load() }
    val ui = vm.ui

    val selectedTheme = ui.themes.firstOrNull { it.selected }?.theme ?: AppTheme.LIGHT
    val bgPainter = settingsBackgroundFor(selectedTheme)

    val sectionTitleColor = when (selectedTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextBody
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = bgPainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Settings", color = sectionTitleColor, fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                BottomBar(
                    selected = 2,
                    onExplore = onOpenExplore,
                    onHome = onOpenHome,
                    onSettings = {  }
                )
            }
        ) { padding ->
            if (ui.loading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color.Black.copy(.04f))
                )
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        "Theme",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = sectionTitleColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    ThemeRow(themes = ui.themes, onClick = vm::selectTheme)

                    Text(
                        "Preferences",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = sectionTitleColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    PreferenceRowSwitch(
                        title = "Sound on Start",
                        checked = ui.soundOnStart,
                        onCheckedChange = { vm.toggleSound(it) }
                    )

                    Text(
                        "Data",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = sectionTitleColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    ActionRowButton(
                        title = "Export",
                        buttonText = if (ui.exporting) "Exporting…" else "Export",
                        enabled = !ui.exporting,
                        onClick = { vm.export(periodDays = 30) }
                    )

                    ActionRowDestructive(
                        title = "Reset All Data",
                        buttonText = "Reset All Data",
                        onClick = vm::askReset
                    )

                    Text(
                        "About",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = sectionTitleColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    AboutRow(onClick = onOpenAbout)

                    Spacer(Modifier.height(18.dp))
                }
            }
        }
    }

    ui.showPaywallFor?.let { pay ->
        PaywallDialog(
            themeTitle = when (pay.theme) {
                AppTheme.WABI -> "Wabi-Sabi Ink"
                AppTheme.KINTSUGI -> "Kintsugi Night"
                else -> ""
            },
            subtitle = when (pay.theme) {
                AppTheme.WABI -> "Calm premium palette"
                AppTheme.KINTSUGI -> "Dark premium palette"
                else -> ""
            },
            price = pay.price ?: "$1.99",
            onUnlock = vm::purchaseSelected,
            onLater = vm::dismissPaywall,
            onRestore = vm::restorePurchases
        )
    }

    if (ui.showResetAlert) {
        ResetFirstDialog(
            onConfirm = vm::confirmResetFirst,
            onCancel  = vm::cancelReset
        )
    }

    if (ui.showResetConfirm) {
        ResetSecondConfirmDialog(
            onYes = vm::confirmResetSecond,
            onNo  = vm::cancelReset
        )
    }

    if (ui.exporting) {
        ExportProgressDialog(onDismiss = { })
    }

    ui.exportLocation?.let {
        ExportSuccessDialog(
            fileName = ui.exportFileName,
            folderHint = exportFolderHint(),
            onOpen = vm::openExport,
            onDismiss = vm::dismissExportDialog
        )
    }

    ui.exportError?.let { msg ->
        ExportErrorDialog(
            message = msg,
            onDismiss = vm::dismissExportDialog
        )
    }
}

@Composable
private fun themePreviewPainter(ui: ThemeUi) = when (ui.theme) {
    AppTheme.LIGHT ->
        if (ui.selected)
            painterResource(Res.drawable.theme_light_selected)
        else
            painterResource(Res.drawable.theme_light_unselected)

    AppTheme.WABI -> {
        if (ui.locked) painterResource(Res.drawable.theme_wabi_locked)
        else if (ui.selected) painterResource(Res.drawable.theme_wabi_unlocked)
        else painterResource(Res.drawable.theme_wabi_unselected)
    }

    AppTheme.KINTSUGI -> {
        if (ui.locked) painterResource(Res.drawable.theme_kintsugi_locked)
        else if (ui.selected) painterResource(Res.drawable.theme_kintsugi_unlocked)
        else painterResource(Res.drawable.theme_kintsugi_unselected)
    }
}

@Composable
private fun ThemeRow(
    themes: List<ThemeUi>,
    onClick: (ThemeUi) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        themes.forEach { t ->
            ThemeTile(ui = t, onClick = { onClick(t) })
        }
    }
}

@Composable
private fun ThemeTile(
    ui: ThemeUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    targetHeight: Dp = 50.dp,
    fallbackSize: DpSize = DpSize(140.dp, 78.dp)
) {
    val painter: Painter = themePreviewPainter(ui)
    val intrinsic = painter.intrinsicSize

    val sizeMod = if (intrinsic.isSpecified && intrinsic.height > 0f) {
        val ratio: Float = intrinsic.width / intrinsic.height
        val w: Dp = targetHeight * ratio
        val h: Dp = targetHeight
        Modifier.requiredSize(w, h)
    } else {
        Modifier.requiredSize(fallbackSize)
    }

    Image(
        painter = painter,
        contentDescription = ui.theme.title,
        modifier = modifier
            .then(sizeMod)
            .clickable(onClick = onClick),
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit
    )
}


@Composable
private fun colorPreview(theme: AppTheme): Color = when (theme) {
    AppTheme.LIGHT    -> Color(0xFFF7E9D3)
    AppTheme.WABI     -> Color(0xFFDA8EA6)
    AppTheme.KINTSUGI -> Color(0xFF6A533E)
}

@Composable
private fun PreferenceRowSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val bgHeight = 70.dp
    val corner = 16.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
            .height(bgHeight)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFFFDCB1), RoundedCornerShape(corner))
                .border(1.dp, HomePalette.ButtonStroke, RoundedCornerShape(corner))
                .padding(horizontal = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = HomePalette.TextTitle,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Image(
                painter = if (checked)
                    painterResource(Res.drawable.ic_sound_on)
                else
                    painterResource(Res.drawable.ic_sound_off),
                contentDescription = if (checked) "Sound on" else "Sound off",
                modifier = Modifier
                    .size(80.dp)
                    .clickable(
                        role = Role.Switch,
                        onClick = { onCheckedChange(!checked) }
                    )
                    .padding(6.dp)
            )
        }
    }
}

private const val PILL_WIDTH_RATIO = 0.40f
private val PILL_HEIGHT = 52.dp

@Composable
private fun ActionRowButton(
    title: String,
    buttonText: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val rowBg = Color(0xFFFFDCB1)
    val pillBg = Color(0xFFFDF3E4)
    val rowCorner = 16.dp
    val pillCorner= 16.dp
    val pillHeight= 52.dp
    val pillWidthRatio = 0.40f

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val pillWidth = maxWidth * pillWidthRatio

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(rowCorner))
                .background(rowBg)
                .border(1.dp, HomePalette.ButtonStroke, RoundedCornerShape(rowCorner))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = HomePalette.TextTitle, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .width(pillWidth)
                    .height(pillHeight)
                    .clip(RoundedCornerShape(pillCorner))
                    .background(pillBg)
                    .border(1.dp, HomePalette.CardStroke, RoundedCornerShape(pillCorner))
                    .clickable(enabled = enabled, onClick = onClick)
                    .alpha(if (enabled) 1f else 0.5f),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_export),
                        contentDescription = "Export",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = buttonText,
                        color = HomePalette.TextTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRowDestructive(
    title: String,
    buttonText: String,
    onClick: () -> Unit
) {
    val rowBg = Color(0xFFFFDCB1)
    val pillBg = Color(0xFFFDF3E4)
    val borderRed = Color(0xFFE05050)
    val rowCorner = 16.dp
    val pillCorner = 16.dp

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val pillWidth = maxWidth * PILL_WIDTH_RATIO

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(rowCorner))
                .background(rowBg)
                .border(1.dp, HomePalette.ButtonStroke, RoundedCornerShape(rowCorner))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = HomePalette.TextTitle, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .width(pillWidth)
                    .height(PILL_HEIGHT)
                    .clip(RoundedCornerShape(pillCorner))
                    .background(pillBg)
                    .border(1.dp, borderRed, RoundedCornerShape(pillCorner))
                    .clickable(role = Role.Button, onClick = onClick)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_reset),
                        contentDescription = "Reset",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(buttonText, color = borderRed, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AboutRow(onClick: () -> Unit) {
    val corner = 16.dp
    val rowMinHeight = 70.dp
    val arrowOrange = Color(0xFFF28C34)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowMinHeight)
            .clip(RoundedCornerShape(corner))
            .background(Color(0xFFFFDCB1))
            .border(1.dp, HomePalette.ButtonStroke, RoundedCornerShape(corner))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "About",
            color = HomePalette.TextTitle,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            "›",
            color = arrowOrange,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp
        )
    }
}

@Composable
fun PaywallDialog(
    themeTitle: String,
    subtitle: String,
    price: String,
    onUnlock: () -> Unit,
    onLater: () -> Unit,
    onRestore: () -> Unit
) {
    val creamOuter   = Color(0xFFE9DDC7)
    val creamInner   = Color(0xFFFDF3E4)
    val orange       = Color(0xFFF0923D)
    val textPrimary  = HomePalette.TextTitle
    val textBody     = HomePalette.TextBody
    val pillStroke   = Color(0xFFD9CBBB)
    val linkColor    = HomePalette.TextBody

    AlertDialog(
        onDismissRequest = onLater,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {},
        dismissButton = {},
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.Transparent,
        text = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .widthIn(min = 320.dp, max = 420.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(creamOuter)
                        .padding(12.dp)
                ) {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(creamInner)
                            .border(1.dp, pillStroke, RoundedCornerShape(24.dp))
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.ic_paywall_bag),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = themeTitle,
                                color = textPrimary,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(subtitle, color = textBody, fontSize = 18.sp)

                        Text(
                            price,
                            color = textPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(orange)
                                .clickable(onClick = onUnlock)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.ic_paywall_lock),
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                )
                                Text(
                                    "Unlock",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(creamInner)
                                .border(1.dp, pillStroke, RoundedCornerShape(22.dp))
                                .clickable(onClick = onLater)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.ic_paywall_clock),
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    "Maybe later",
                                    color = textPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        TextButton(
                            onClick = onRestore,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                "Restore purchases",
                                color = linkColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ExportProgressDialog(
    onDismiss: () -> Unit = {}
) {
    val creamOuter  = Color(0xFFE9DDC7)
    val creamInner  = Color(0xFFFDF3E4)
    val pillStroke  = Color(0xFFD9CBBB)
    val orange      = Color(0xFFF0923D)
    val textPrimary = HomePalette.TextTitle

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {},
        dismissButton = {},
        containerColor = Color.Transparent,
        shape = RoundedCornerShape(28.dp),
        text = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .widthIn(min = 300.dp, max = 380.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(creamOuter)
                        .padding(12.dp)
                ) {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(creamInner)
                            .border(1.dp, pillStroke, RoundedCornerShape(24.dp))
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "Exporting…",
                            color = textPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 3.dp,
                                color = orange,
                                trackColor = orange.copy(alpha = 0.18f)
                            )
                            Text(
                                "Generating PDF, please wait",
                                color = textPrimary
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ExportSuccessDialog(
    fileName: String?,
    folderHint: String,
    onOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    val creamOuter  = Color(0xFFE9DDC7)
    val creamInner  = Color(0xFFFDF3E4)
    val pillStroke  = Color(0xFFD9CBBB)
    val orange      = Color(0xFFF0923D)
    val textPrimary = HomePalette.TextTitle
    val textBody    = HomePalette.TextBody

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        containerColor = Color.Transparent,
        shape = RoundedCornerShape(28.dp),
        text = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .widthIn(min = 300.dp, max = 380.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(creamOuter)
                        .padding(12.dp)
                ) {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(creamInner)
                            .border(1.dp, pillStroke, RoundedCornerShape(24.dp))
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "Export complete",
                            color = textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Saved to $folderHint",
                            color = textBody
                        )
                        fileName?.let {
                            Text(it, color = textPrimary, fontWeight = FontWeight.Medium)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(orange)
                                    .clickable(onClick = onOpen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Open",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(creamInner)
                                    .border(1.dp, pillStroke, RoundedCornerShape(18.dp))
                                    .clickable(onClick = onDismiss),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "OK",
                                    color = textPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ExportErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    val creamOuter  = Color(0xFFE9DDC7)
    val creamInner  = Color(0xFFFDF3E4)
    val pillStroke  = Color(0xFFD9CBBB)
    val red         = Color(0xFFE05050)
    val textPrimary = HomePalette.TextTitle

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        containerColor = Color.Transparent,
        shape = RoundedCornerShape(28.dp),
        text = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .widthIn(min = 300.dp, max = 380.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(creamOuter)
                        .padding(12.dp)
                ) {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(creamInner)
                            .border(1.dp, pillStroke, RoundedCornerShape(24.dp))
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "Export failed",
                            color = red,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(message, color = textPrimary)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(creamInner)
                                .border(1.dp, pillStroke, RoundedCornerShape(18.dp))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "OK",
                                color = textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ResetFirstDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val creamOuter  = Color(0xFFE9DDC7)
    val creamInner  = Color(0xFFFDF3E4)
    val stroke      = Color(0xFFD9CBBB)
    val red         = Color(0xFFE15555)
    val textPrimary = HomePalette.TextTitle
    val textBody    = HomePalette.TextBody

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            val cardWidth = maxWidth
                .coerceAtMost(420.dp)
                .coerceAtLeast(320.dp)

            Box(
                modifier = Modifier
                    .width(cardWidth)
                    .clip(RoundedCornerShape(28.dp))
                    .background(creamOuter)
                    .padding(12.dp)
                    .align(Alignment.Center)
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(creamInner)
                        .border(1.dp, stroke, RoundedCornerShape(24.dp))
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(Res.drawable.illustration_reset_dog),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth(0.50f)
                            .height(100.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Reset all data?",
                        color = textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "This will permanently delete your ideas and journal.",
                        color = textBody,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(red)
                            .clickable(onClick = onConfirm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Reset",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(creamInner)
                            .border(2.dp, red, RoundedCornerShape(22.dp))
                            .clickable(onClick = onCancel),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Cancel",
                            color = textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetSecondConfirmDialog(
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    val creamInner  = Color(0xFFFDF3E4)
    val stroke      = Color(0xFFD9CBBB)
    val red         = Color(0xFFE15555)
    val textPrimary = HomePalette.TextTitle

    AlertDialog(
        onDismissRequest = onNo,
        confirmButton = {},
        dismissButton = {},
        containerColor = Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        text = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    Modifier
                        .align(Alignment.Center)
                        .widthIn(max = 360.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(creamInner)
                        .border(1.dp, stroke, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Delete?",
                        color = textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(red)
                                .clickable(onClick = onYes),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Yes", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(creamInner)
                                .border(1.dp, stroke, RoundedCornerShape(22.dp))
                                .clickable(onClick = onNo),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No", color = textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    )
}