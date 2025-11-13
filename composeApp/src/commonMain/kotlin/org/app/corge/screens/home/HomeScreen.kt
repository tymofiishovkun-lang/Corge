package org.app.corge.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.bg_home_kintsugi
import corge.composeapp.generated.resources.bg_home_kintsugi_loading
import corge.composeapp.generated.resources.bg_home_light
import corge.composeapp.generated.resources.bg_home_light_loading
import corge.composeapp.generated.resources.bg_home_wabi_loading
import corge.composeapp.generated.resources.bg_settings_kintsugi
import corge.composeapp.generated.resources.bg_settings_light
import corge.composeapp.generated.resources.bg_settings_wabi
import corge.composeapp.generated.resources.breathing_card_image
import corge.composeapp.generated.resources.dog_card_image
import corge.composeapp.generated.resources.ic_done
import corge.composeapp.generated.resources.ic_explore_1
import corge.composeapp.generated.resources.ic_explore_2
import corge.composeapp.generated.resources.ic_explore_3
import corge.composeapp.generated.resources.ic_explore_4
import corge.composeapp.generated.resources.ic_explore_5
import corge.composeapp.generated.resources.ic_explore_6
import corge.composeapp.generated.resources.ic_favorites
import corge.composeapp.generated.resources.ic_favorites_kintsugi
import corge.composeapp.generated.resources.ic_journal
import corge.composeapp.generated.resources.ic_journal_kintsugi
import corge.composeapp.generated.resources.ic_not_done
import corge.composeapp.generated.resources.ic_note
import corge.composeapp.generated.resources.ic_read_aloud
import corge.composeapp.generated.resources.ic_start_sound
import corge.composeapp.generated.resources.ic_stats
import corge.composeapp.generated.resources.ic_stats_kintsugi
import corge.composeapp.generated.resources.ic_stop_sound
import kotlinx.coroutines.delay
import org.app.corge.data.model.Category
import org.app.corge.data.model.MessageType
import org.app.corge.data.repository.SettingsRepository
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.screens.bottomBar.BottomBar
import org.app.corge.screens.settings.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

object HomePalette {
    val CardBg = Color(0xFFF6EDDF)
    val CardStroke = Color(0xFFEFD9BF)
    val TagPill = Color(0xFFF6D7B8)
    val TagText = Color(0xFF7D5A3A)
    val Primary = Color(0xFFF79B3D)
    val TextTitle = Color(0xFF6B4A2F)
    val TextBody = Color(0xFF5B4A3E)
    val ButtonBg = Color(0xFFEFE6DA)
    val ButtonStroke = Color(0xFFDCCAB4)
    val QuickIconBg = Color(0xFFF1E6D9)
    val ChipBg = Color(0xFFEAD8C7)
}

object HomeDimens {
    val ScreenPad = 16.dp
    val BigCardHeight = 336.dp
    val BigCardRadius = 16.dp
    val InnerPad = 16.dp
    val ButtonHeight = 40.dp
    val AddNoteHeight = 44.dp
    val QuickIconSize = 44.dp
    val ExploreCardH = 108.dp
    val ExploreCardRadius = 14.dp
    val ChipHeight = 34.dp
    val ChipRadius = 18.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenJournal: () -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    onOpenStats: () -> Unit = {},
    onOpenMessageDetails: (Long) -> Unit = {},
    onOpenCategory: (String) -> Unit = {},
    onOpenExplore: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    settingsRepository: SettingsRepository,
    viewModel: HomeViewModel = koinInject(),
    themeRepo: ThemeRepository = koinInject()
) {
    val ui by viewModel.uiState.collectAsState()

    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT
    val isFirstHomeStart by produceState(initialValue = false) {
        value = settingsRepository.isFirstHomeStart()
    }

    LaunchedEffect(isFirstHomeStart) {
        if (isFirstHomeStart) settingsRepository.setFirstHomeStart(false)
    }

    val bgPainter = when (appTheme) {
        AppTheme.LIGHT    -> painterResource(Res.drawable.bg_settings_light)
        AppTheme.WABI     -> painterResource(Res.drawable.bg_settings_wabi)
        AppTheme.KINTSUGI -> painterResource(Res.drawable.bg_settings_kintsugi)
    }

    val sectionTitleColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextBody
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = bgPainter,
            contentDescription = "Home background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (ui !is HomeUiState.Loading) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Today",
                                color = sectionTitleColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            },
            bottomBar = {
                BottomBar(
                    selected = 1,
                    onExplore = onOpenExplore,
                    onHome = {  },
                    onSettings = onOpenSettings
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Crossfade(
                targetState = ui,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = HomeDimens.ScreenPad)
            ) { state ->
                when (state) {
                    is HomeUiState.Loading -> HomeLoading(appTheme, isFirstHomeStart)
                    is HomeUiState.Empty -> HomeEmpty(onStart = { viewModel.startFirstSession() }, appTheme)
                    is HomeUiState.Loaded -> HomeContent(
                        state = state,
                        onReadAloud = { viewModel.readAloud() },
                        onToggleSound = { viewModel.toggleSound() },
                        onMarkDone = { viewModel.toggleDone() },
                        onAddNote = { text -> viewModel.saveNote(text) },
                        onOpenJournal = onOpenJournal,
                        onOpenFavorites = onOpenFavorites,
                        onOpenStats = onOpenStats,
                        onOpenMessageDetails = onOpenMessageDetails,
                        onOpenCategory = onOpenCategory
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeLoading(
    appTheme: AppTheme,
    isFirstHomeStart: Boolean
) {
    if (isFirstHomeStart) {
        // 🔹 вариант для самого первого запуска
        val bgPainter = when (appTheme) {
            AppTheme.LIGHT    -> painterResource(Res.drawable.bg_settings_light)
            AppTheme.WABI     -> painterResource(Res.drawable.bg_settings_wabi)
            AppTheme.KINTSUGI -> painterResource(Res.drawable.bg_settings_kintsugi)
        }

        var showLoader by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            delay(2000)
            showLoader = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Image(
                painter = bgPainter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            if (showLoader) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = HomePalette.Primary,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    } else {
        val bgPainter = when (appTheme) {
            AppTheme.LIGHT    -> painterResource(Res.drawable.bg_home_light_loading)
            AppTheme.WABI     -> painterResource(Res.drawable.bg_home_wabi_loading)
            AppTheme.KINTSUGI -> painterResource(Res.drawable.bg_home_kintsugi_loading)
        }

        var visible by remember { mutableStateOf(false) }

        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            label = "bgFade"
        )

        LaunchedEffect(Unit) {
            visible = true
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Image(
                painter = bgPainter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha),
                contentScale = ContentScale.Crop
            )

            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(HomeDimens.BigCardHeight)
                        .clip(RoundedCornerShape(HomeDimens.BigCardRadius))
                        .background(HomePalette.CardBg.copy(alpha = .6f))
                        .border(
                            1.dp,
                            HomePalette.CardStroke,
                            RoundedCornerShape(HomeDimens.BigCardRadius)
                        )
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(HomeDimens.AddNoteHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HomePalette.ButtonBg.copy(alpha = .6f))
                        .border(
                            1.dp,
                            HomePalette.ButtonStroke,
                            RoundedCornerShape(12.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun HomeEmpty(
    onStart: () -> Unit,
    appTheme: AppTheme
) {
    val bgPainter = when (appTheme) {
        AppTheme.LIGHT,
        AppTheme.WABI -> painterResource(Res.drawable.bg_home_light)
        AppTheme.KINTSUGI -> painterResource(Res.drawable.bg_home_kintsugi)
    }
    val sectionTitleColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextBody
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = bgPainter,
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "Start your spiritual growth now.",
            style = MaterialTheme.typography.titleMedium.copy(color = sectionTitleColor),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = HomePalette.Primary),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier
                .width(300.dp)
                .height(50.dp)
        ) {
            Text(
                "Start",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Loaded,
    onReadAloud: () -> Unit,
    onToggleSound: () -> Unit,
    onMarkDone: () -> Unit,
    onAddNote: (String) -> Unit,
    onOpenJournal: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenMessageDetails: (Long) -> Unit,
    onOpenCategory: (String) -> Unit,
    themeRepo: ThemeRepository = koinInject()
) {
    var noteDialog by remember { mutableStateOf(false) }
    var noteText by remember(state.note) { mutableStateOf(state.note.orEmpty()) }
    val scroll = rememberScrollState()

    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT

    val (journalIcon, favoritesIcon, statsIcon) = when (appTheme) {
        AppTheme.KINTSUGI -> Triple(
            painterResource(Res.drawable.ic_journal_kintsugi),
            painterResource(Res.drawable.ic_favorites_kintsugi),
            painterResource(Res.drawable.ic_stats_kintsugi)
        )
        else -> Triple(
            painterResource(Res.drawable.ic_journal),
            painterResource(Res.drawable.ic_favorites),
            painterResource(Res.drawable.ic_stats)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val tag = when (state.message.type) {
            MessageType.TASK -> "Task"
            MessageType.BREATHING -> "Breathing"
            MessageType.PHRASE -> "Phrase"
        }

        BigCard(
            title = state.message.category,
            tag = tag,
            text = state.message.textEn,
            isDone = state.isDoneToday,
            isSoundPlaying = state.isSoundPlaying,
            onClick = { onOpenMessageDetails(state.message.id) },
            onReadAloud = onReadAloud,
            onToggleSound = onToggleSound,
            onAddNote = { noteDialog = true }
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickImageAction(
                painter = journalIcon,
                contentDescription = "Journal",
                onClick = onOpenJournal
            )
            QuickImageAction(
                painter = favoritesIcon,
                contentDescription = "Favorites",
                onClick = onOpenFavorites
            )
            QuickImageAction(
                painter = statsIcon,
                contentDescription = "Stats",
                onClick = onOpenStats
            )
        }

        Spacer(Modifier.height(14.dp))

        CategoryGrid(state.categories, onOpenCategory)


        Spacer(Modifier.height(16.dp))

        val exploreImages = listOf(
            painterResource(Res.drawable.ic_explore_1),
            painterResource(Res.drawable.ic_explore_2),
            painterResource(Res.drawable.ic_explore_3),
            painterResource(Res.drawable.ic_explore_4),
            painterResource(Res.drawable.ic_explore_5),
            painterResource(Res.drawable.ic_explore_6)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.explore.take(2).forEachIndexed { index, line ->
                ExploreCard(
                    text = line,
                    imageRes = exploreImages[index % exploreImages.size],
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (noteDialog) {
        NoteDialog(
            noteText = noteText,
            onNoteChange = { noteText = it },
            onSave = { onAddNote(noteText); noteDialog = false },
            onCancel = { noteDialog = false }
        )
    }
}

@Composable
private fun NoteDialog(
    noteText: String,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {},
        dismissButton = {},
        title = {},
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFFFFFCF8),
        modifier = Modifier.widthIn(max = 360.dp),
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 340.dp)
                    .background(Color(0xFFFFFCF8), RoundedCornerShape(28.dp))
                    .padding(horizontal = 22.dp, vertical = 24.dp)
            ) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { if (it.length <= 280) onNoteChange(it) },
                    placeholder = { Text("Notes", color = Color(0xFF7A5E45), fontSize = 15.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE5CBAA),
                        unfocusedContainerColor = Color(0xFFE5CBAA),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color(0xFF7A5E45)
                    ),
                    textStyle = LocalTextStyle.current.copy(color = Color(0xFF3A2E1E), fontSize = 15.sp),
                    singleLine = false, minLines = 3, maxLines = 6
                )

                Spacer(Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA24C), contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(3.dp, pressedElevation = 5.dp)
                    ) { Text("Save", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }

                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFDF3E4), contentColor = Color(0xFF3A2E1E)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(3.dp, pressedElevation = 5.dp)
                    ) { Text("Cancel", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
                }
            }
        }
    )
}

@Composable
private fun QuickImageAction(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.size(64.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun BigCard(
    title: String,
    tag: String,
    text: String,
    isDone: Boolean,
    isSoundPlaying: Boolean,
    onClick: () -> Unit,
    onReadAloud: () -> Unit,
    onToggleSound: () -> Unit,
    onAddNote: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HomeDimens.BigCardRadius))
            .background(HomePalette.CardBg)
            .border(1.dp, HomePalette.CardStroke, RoundedCornerShape(HomeDimens.BigCardRadius))
            .clickable(onClick = onClick)
            .padding(HomeDimens.InnerPad)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = HomePalette.TextTitle,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = .2.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))

            val imageRes = when (tag) {
                "Task", "Phrase" -> Res.drawable.dog_card_image
                "Breathing"      -> Res.drawable.breathing_card_image
                else             -> Res.drawable.dog_card_image
            }

            Image(
                painter = painterResource(imageRes),
                contentDescription = "Card illustration",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(HomePalette.TagPill)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    tag,
                    color = HomePalette.TagText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(color = HomePalette.TextBody),
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PillButton(
                    text = "Read Aloud",
                    leadingIcon = painterResource(Res.drawable.ic_read_aloud),
                    backgroundColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = onReadAloud
                )

                val soundIcon = if (isSoundPlaying)
                    painterResource(Res.drawable.ic_stop_sound)
                else
                    painterResource(Res.drawable.ic_start_sound)

                val soundText = if (isSoundPlaying) "Stop Sound" else "Start Sound"

                PillButton(
                    text = soundText,
                    leadingIcon = soundIcon,
                    backgroundColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = onToggleSound
                )
            }

            Spacer(Modifier.height(10.dp))

            PillButton(
                text = "Add Note",
                leadingIcon = painterResource(Res.drawable.ic_note),
                backgroundColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HomeDimens.AddNoteHeight),
                onClick = onAddNote
            )
        }

        val badgeIcon = if (isDone)
            painterResource(Res.drawable.ic_done)
        else
            painterResource(Res.drawable.ic_not_done)

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = badgeIcon,
                contentDescription = if (isDone) "Done" else "Not done",
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun PillButton(
    text: String,
    leadingIcon: Painter? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = HomePalette.ButtonBg,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(HomeDimens.ButtonHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, HomePalette.ButtonStroke, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leadingIcon != null) {
            Image(
                painter = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(6.dp))
        }

        Text(
            text,
            style = MaterialTheme.typography.labelLarge.copy(color = HomePalette.TextTitle)
        )
    }
}

@Composable
private fun QuickAction(icon: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 80.dp).clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .size(HomeDimens.QuickIconSize)
                .clip(RoundedCornerShape(12.dp))
                .background(HomePalette.QuickIconBg),
            contentAlignment = Alignment.Center
        ) { Text(icon) }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium.copy(color = HomePalette.TextBody))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryGrid(
    categories: List<Category>,
    onOpenResults: (String) -> Unit
) {
    val chipWidth = 110.dp
    val chipHeight = 50.dp
    val chipCorner = 16.dp
    val chipBgColor = Color(0xFFE4C8A7)
    val chipTextColor = Color.Black

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.forEach { cat ->
            Box(
                modifier = Modifier
                    .width(chipWidth)
                    .height(chipHeight)
                    .clip(RoundedCornerShape(chipCorner))
                    .background(chipBgColor)
                    .clickable { onOpenResults(cat.id) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cat.title,
                    color = chipTextColor,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ExploreCard(
    text: String,
    imageRes: Painter,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(HomeDimens.ExploreCardRadius))
            .border(
                width = 1.dp,
                color = HomePalette.CardStroke,
                shape = RoundedCornerShape(HomeDimens.ExploreCardRadius)
            )
            .background(HomePalette.CardBg)
            .padding(12.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF703F03).copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = imageRes,
                contentDescription = null,
                modifier = Modifier.size(70.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
