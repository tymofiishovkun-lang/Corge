package org.app.corge.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.bg_settings_kintsugi
import corge.composeapp.generated.resources.bg_settings_light
import corge.composeapp.generated.resources.bg_settings_wabi
import corge.composeapp.generated.resources.breathing_card_image
import corge.composeapp.generated.resources.dog_card_image
import corge.composeapp.generated.resources.ic_back
import corge.composeapp.generated.resources.ic_back_kintsugi
import corge.composeapp.generated.resources.ic_bell
import corge.composeapp.generated.resources.ic_done
import corge.composeapp.generated.resources.ic_fav
import corge.composeapp.generated.resources.ic_fav_filled
import corge.composeapp.generated.resources.ic_not_done
import corge.composeapp.generated.resources.ic_share
import corge.composeapp.generated.resources.ic_start_sound
import corge.composeapp.generated.resources.ic_stop_sound
import corge.composeapp.generated.resources.ic_time
import org.app.corge.PlatformHandler
import org.app.corge.data.model.Message
import org.app.corge.data.model.MessageType
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.screens.home.HomeDimens
import org.app.corge.screens.home.HomePalette
import org.app.corge.screens.settings.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    messageId: Long,
    onBack: () -> Unit,
    onStartSession: (Long) -> Unit,
    vm: DetailsViewModel = koinInject(),
    themeRepo: ThemeRepository = koinInject(),
    platformHandler: PlatformHandler,
    dateArg: String? = null
) {
    LaunchedEffect(messageId, dateArg) {
        vm.load(messageId, dateArg)
    }

    val ui = vm.ui

    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT

    val bgPainter = when (appTheme) {
        AppTheme.LIGHT    -> painterResource(Res.drawable.bg_settings_light)
        AppTheme.WABI     -> painterResource(Res.drawable.bg_settings_wabi)
        AppTheme.KINTSUGI -> painterResource(Res.drawable.bg_settings_kintsugi)
    }

    val sectionTitleColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextTitle
    }

    val backIcon = when (appTheme) {
        AppTheme.KINTSUGI -> painterResource(Res.drawable.ic_back_kintsugi)
        else               -> painterResource(Res.drawable.ic_back)
    }

    val favOnIcon = painterResource(Res.drawable.ic_fav_filled)
    val favOffIcon = painterResource(Res.drawable.ic_fav)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = bgPainter,
            contentDescription = "Details background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = backIcon,
                                contentDescription = "Back",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    actions = {
                        if (ui is DetailsUiState.Loaded) {
                            IconButton(
                                onClick = vm::toggleFavorite,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = if (ui.isFavorite) favOnIcon else favOffIcon,
                                    contentDescription = "Toggle favorite",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    },
                    title = {
                        val title = (ui as? DetailsUiState.Loaded)?.message?.category ?: "Details"
                        Text(
                            title,
                            color = sectionTitleColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (ui) {
                    DetailsUiState.Loading -> LoadingShimmer()
                    DetailsUiState.Error -> ErrorStub(onBack)
                    is DetailsUiState.Loaded -> DetailsContent(
                        st = ui,
                        vm = vm,
                        onStartSession = onStartSession,
                        dateArg = dateArg,
                        platformHandler = platformHandler
                    )
                }

                if (ui is DetailsUiState.Loaded && ui.isBusy) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.08f))
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsContent(
    st: DetailsUiState.Loaded,
    vm: DetailsViewModel,
    onStartSession: (Long) -> Unit,
    platformHandler: PlatformHandler,
    dateArg: String?
) {
    val msg = st.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MessageCard(
            msg = msg,
            isDone = st.isDoneToday,
            onClick = {}
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            msg.durationSeconds?.takeIf { it > 0 }?.let { sec ->
                val m = sec / 60; val s = sec % 60
                MetaChip(
                    text = if (m > 0) "${m}m${if (s > 0) " ${s}s" else ""}" else "${s}s",
                    iconRes = Res.drawable.ic_time
                )
            }

            msg.recommendedTime?.takeIf { it.isNotBlank() }?.let {
                MetaChip(text = it, iconRes = Res.drawable.ic_time)
            }
        }

        val ritualText = when (msg.type) {
            MessageType.TASK -> msg.ritual?.trim().takeUnless { it.isNullOrEmpty() }
                ?: "You can do this task at any time of the day, the main thing is calmness and concentration."
            MessageType.BREATHING -> msg.ritual?.trim().takeUnless { it.isNullOrEmpty() }
                ?: "Breathe slowly and naturally. If you feel dizzy, pause."
            MessageType.PHRASE -> ""
        }.orEmpty()

        if (ritualText.isNotBlank()) {
            InfoRow(
                iconRes = Res.drawable.ic_time,
                text = ritualText,
                backgroundColor = Color(0xFFD8B892),
                borderColor = Color(0xFF7B5A3A)
            )
        }

        val whyText = msg.whyItMatters?.trim().takeUnless { it.isNullOrEmpty() }
            ?: "This is important for your inner peace"

        InfoRow(
            iconRes = Res.drawable.ic_bell,
            text = whyText,
            backgroundColor = Color(0xFFF6E1CA),
            borderColor = Color(0xFFF79B3D)
        )

        NoteField(
            value = st.note,
            onValueChange = vm::onNoteChange,
            onSave = vm::saveNote
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val soundIcon = if (st.isSoundOn)
                painterResource(Res.drawable.ic_stop_sound)
            else
                painterResource(Res.drawable.ic_start_sound)

            val soundText = if (st.isSoundOn) "Stop Sound" else "Start Sound"

            PillButton(
                text = soundText,
                leadingIcon = soundIcon,
                backgroundColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = vm::toggleSound
            )

            val shareIcon = painterResource(Res.drawable.ic_share)

            PillButton(
                text = "Share",
                leadingIcon = shareIcon,
                backgroundColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = {
                    val msg = st.message
                    val shareText = buildString {
                        appendLine("✨ ${msg.category}")
                        appendLine()
                        appendLine(msg.textEn)
                        if (!msg.whyItMatters.isNullOrBlank()) {
                            appendLine()
                            appendLine(" Why it matters:")
                            appendLine(msg.whyItMatters)
                        }
                        if (!msg.ritual.isNullOrBlank()) {
                            appendLine()
                            appendLine(" Ritual:")
                            appendLine(msg.ritual)
                        }
                    }

                    val filePath = vm.createShareFileForMessage(shareText)
                    platformHandler.shareFile(filePath)
                }
            )
        }

        val primaryLabel = "Start"
        val isDisabled = st.isDoneToday

        Button(
            onClick = { if (!isDisabled) onStartSession(msg.id) },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDisabled)
                    HomePalette.Primary.copy(alpha = 0.4f)
                else
                    HomePalette.Primary
            ),
            enabled = !isDisabled,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(primaryLabel, color = Color.White)
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun PillButton(
    text: String,
    leadingIcon: Painter? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = HomePalette.ButtonBg,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, HomePalette.ButtonStroke, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        leadingIcon?.let {
            Image(
                painter = it,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(6.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = HomePalette.TextTitle,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun MessageCard(
    msg: Message,
    isDone: Boolean,
    onClick: () -> Unit
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
                text = msg.category,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = HomePalette.TextTitle,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = .2.sp
                )
            )

            Spacer(Modifier.height(6.dp))

            val imageRes = when (msg.type) {
                MessageType.TASK,
                MessageType.PHRASE -> Res.drawable.dog_card_image
                MessageType.BREATHING -> Res.drawable.breathing_card_image
            }

            Image(
                painter = painterResource(imageRes),
                contentDescription = "Message illustration",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(8.dp))

            val typeLabel = when (msg.type) {
                MessageType.TASK      -> "Task"
                MessageType.BREATHING -> "Breathing"
                MessageType.PHRASE    -> "Phrase"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(HomePalette.TagPill)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = typeLabel,
                    color = HomePalette.TagText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = msg.textEn,
                style = MaterialTheme.typography.bodyMedium.copy(color = HomePalette.TextBody),
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
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
private fun MetaChip(
    text: String,
    iconRes: DrawableResource? = null
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(HomePalette.TagPill)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            iconRes?.let {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    tint = HomePalette.TagText,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text,
                color = HomePalette.TagText,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun InfoRow(
    iconRes: DrawableResource,
    text: String,
    backgroundColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = text,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun NoteField(
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 280) onValueChange(it) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            shape = RoundedCornerShape(18.dp),
            placeholder = {
                if (!isFocused && value.isEmpty()) {
                    Text("No notes", color = Color(0xFF5B4A3E).copy(alpha = 0.6f))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF703F03),
                unfocusedBorderColor = Color(0xFF703F03),
                cursorColor = Color(0xFF703F03),
                focusedContainerColor = Color(0xFFD8B892),
                unfocusedContainerColor = Color(0xFFD8B892)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF703F03), RoundedCornerShape(18.dp))
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                }
        )

        Button(
            onClick = {
                focusManager.clearFocus()
                onSave()
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF79B3D),
                contentColor = Color.Black
            ),
            modifier = Modifier
                .height(42.dp)
                .align(Alignment.End)
        ) {
            Text(
                "Save note",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LoadingShimmer() {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .06f)))
}

@Composable
private fun ErrorStub(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nothing here", color = HomePalette.TextBody)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, shape = RoundedCornerShape(12.dp)) { Text("Back") }
    }
}
