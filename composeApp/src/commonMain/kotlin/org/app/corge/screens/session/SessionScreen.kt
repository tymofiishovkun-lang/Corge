package org.app.corge.screens.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.bg_settings_kintsugi
import corge.composeapp.generated.resources.bg_settings_light
import corge.composeapp.generated.resources.bg_settings_wabi
import corge.composeapp.generated.resources.breathing_card_image
import corge.composeapp.generated.resources.dog_card_image
import corge.composeapp.generated.resources.ic_back
import corge.composeapp.generated.resources.ic_back_kintsugi
import corge.composeapp.generated.resources.ic_done
import corge.composeapp.generated.resources.ic_done_brown
import corge.composeapp.generated.resources.ic_not_done
import corge.composeapp.generated.resources.ic_start_sound
import corge.composeapp.generated.resources.ic_steps
import corge.composeapp.generated.resources.ic_stop_sound
import org.app.corge.data.model.Message
import org.app.corge.data.model.MessageType
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.screens.detail.PillButton
import org.app.corge.screens.home.HomeDimens
import org.app.corge.screens.home.HomePalette
import org.app.corge.screens.settings.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    messageId: Long,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    vm: SessionViewModel = koinInject(),
    themeRepo: ThemeRepository = koinInject()
) {
    LaunchedEffect(messageId) { vm.load(messageId) }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = bgPainter,
            contentDescription = "Session background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (ui is SessionViewModel.UiState.Loaded) vm.askExitConfirm()
                                else onBack()
                            },
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
                    title = {
                        Text(
                            "Session",
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
            when (ui) {
                SessionViewModel.UiState.Loading -> Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color.Black.copy(alpha = 0.04f))
                )

                SessionViewModel.UiState.Error -> Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Something went wrong", color = HomePalette.TextBody)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Back") }
                }

                is SessionViewModel.UiState.Loaded -> {
                    val st = ui as SessionViewModel.UiState.Loaded
                    SessionContent(st = st, padding = padding, vm = vm, onFinished = onFinished)

                    if (st.showExitConfirm) {
                        Dialog(onDismissRequest = vm::cancelExitConfirm) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 24.dp, vertical = 20.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    Text(
                                        "Want to leave the session?",
                                        color = Color(0xFF6B4A2F),
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = {
                                                vm.resetSessionState()
                                                onBack()
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = HomePalette.Primary,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Yes")
                                        }

                                        OutlinedButton(
                                            onClick = vm::cancelExitConfirm,
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.dp, Color(0xFF6B4A2F)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B4A2F)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("No")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionContent(
    st: SessionViewModel.UiState.Loaded,
    padding: PaddingValues,
    vm: SessionViewModel,
    onFinished: () -> Unit
) {
    val msg = st.message
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MessageCard(
            msg = msg,
            onClick = {}
        )

        StepsBlock(
            steps = st.steps,
            stepIndex = st.stepIndex,
            isExpanded = st.isStepsExpanded,
            onToggleExpand = vm::toggleStepsExpand,
            onNext = vm::nextStep
        )

        BreathingAndTimer(
            isBreathing = st.isBreathing,
            elapsedSec = st.elapsedSec,
            targetSec = st.targetSec,
            isPlaying = st.isPlayingSound,
            onPlayPause = vm::toggleSound,
            onStartTimer = vm::startTimer,
            onPauseTimer = vm::pauseTimer
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val soundIcon = if (st.isPlayingSound)
                painterResource(Res.drawable.ic_stop_sound)
            else
                painterResource(Res.drawable.ic_start_sound)

            val soundText = if (st.isPlayingSound) "Pause Sound" else "Start Sound"

            PillButton(
                text = soundText,
                leadingIcon = soundIcon,
                backgroundColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = vm::toggleSound
            )

            Button(
                onClick = vm::showDoneDialog,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HomePalette.Primary),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Text("Done", color = Color.White)
            }
        }
    }

    if (st.showDoneDialog) {
        Dialog(onDismissRequest = vm::hideDoneDialog) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.ic_done_brown),
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Well done",
                            color = Color(0xFF7B4A08),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                    }

                    OutlinedTextField(
                        value = st.noteDraft,
                        onValueChange = vm::editNote,
                        placeholder = { Text("Notes", color = Color(0xFF9F7E56)) },
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8D1AC),
                            unfocusedContainerColor = Color(0xFFE8D1AC),
                            focusedIndicatorColor = Color(0xFF9F7E56),
                            unfocusedIndicatorColor = Color(0xFF9F7E56),
                            cursorColor = Color(0xFF9F7E56),
                            focusedTextColor = Color(0xFF5C3A1E),
                            unfocusedTextColor = Color(0xFF5C3A1E)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFF89B4A))
                                .clickable { vm.saveDoneAndNote(onFinished) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Save",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFFCF9F2))
                                .border(1.dp, Color(0xFFD7C6A8), RoundedCornerShape(24.dp))
                                .clickable(onClick = vm::hideDoneDialog),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Cancel",
                                color = Color(0xFF5C3A1E),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageCard(
    msg: Message,
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
                    letterSpacing = 0.2.sp
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
                    .size(84.dp)
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
    }
}

@Composable
private fun StepsBlock(
    steps: List<String>,
    stepIndex: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HomePalette.ChipBg)
            .border(1.dp, HomePalette.CardStroke, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_steps),
                contentDescription = "Steps",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 10.dp)
            )

            Text(
                text = "Steps",
                color = HomePalette.TextTitle,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Text(if (isExpanded) "▾" else "▸")
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                if (steps.isNotEmpty()) {
                    Text(
                        "${stepIndex + 1}. ${steps[stepIndex]}",
                        color = HomePalette.TextBody
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onNext,
                            shape = RoundedCornerShape(10.dp),
                            enabled = stepIndex < steps.lastIndex,
                            border = BorderStroke(1.dp, Color.Black),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                        ) {
                            Text(
                                text = if (stepIndex < steps.lastIndex) "Next" else "Done",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreathingAndTimer(
    isBreathing: Boolean,
    elapsedSec: Int,
    targetSec: Int?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    themeRepo: ThemeRepository = koinInject()
) {

    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT

    val sectionTitleColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextTitle
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isBreathing) {
            val infinite = rememberInfiniteTransition(label = "breath")
            val scale by infinite.animateFloat(
                initialValue = 0.85f, targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "breathScale"
            )
            Box(
                Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .background(HomePalette.TagPill),
                contentAlignment = Alignment.Center
            ) {
                Text("Breathe", color = HomePalette.TagText, fontWeight = FontWeight.SemiBold)
            }
        }

        val t = formatTime(elapsedSec) + targetSec?.let { " / ${formatTime(it)}" }.orEmpty()
        Text(t, color = sectionTitleColor)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onStartTimer,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text("Start timer", color = Color.Black, fontWeight = FontWeight.Medium)
            }

            OutlinedButton(
                onClick = onPauseTimer,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text("Pause", color = Color.Black, fontWeight = FontWeight.Medium)
            }
        }
    }
}
private fun formatTime(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

