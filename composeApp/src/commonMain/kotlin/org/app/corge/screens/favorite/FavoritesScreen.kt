package org.app.corge.screens.favorite

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.bg_home_kintsugi
import corge.composeapp.generated.resources.bg_home_light
import corge.composeapp.generated.resources.bg_settings_kintsugi
import corge.composeapp.generated.resources.bg_settings_light
import corge.composeapp.generated.resources.bg_settings_wabi
import corge.composeapp.generated.resources.breathing_card_image
import corge.composeapp.generated.resources.dog_card_image
import corge.composeapp.generated.resources.ic_back
import corge.composeapp.generated.resources.ic_back_kintsugi
import corge.composeapp.generated.resources.ic_delete
import kotlinx.coroutines.launch
import org.app.corge.data.model.Message
import org.app.corge.data.model.MessageType
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.screens.home.HomePalette
import org.app.corge.screens.settings.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onOpenDetails: (Long, String?) -> Unit,
    vm: FavoritesViewModel = koinInject(),
    themeRepo: ThemeRepository = koinInject()
) {
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
            contentDescription = "Favorites background",
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
                    title = {
                        Text(
                            "Favorites",
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
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (ui) {
                    FavoritesUiState.Loading -> {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.04f))
                        )
                    }

                    FavoritesUiState.Empty -> {
                        val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
                        val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT

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
                                "You don't have a favorite yet.",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = sectionTitleColor,
                                    textAlign = TextAlign.Center
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is FavoritesUiState.Loaded -> FavoritesList(
                        items = ui.items,
                        onOpen = { id, _ ->
                            val date = ui.lastDateById[id]?.ifBlank { null }
                            onOpenDetails(id, date)
                        },
                        onRemove = vm::removeFromFavorites
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesList(
    items: List<Message>,
    onOpen: (Long, String?) -> Unit,
    onRemove: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { m ->
            SwipeDismissItem(
                onDismiss = { onRemove(m.id) },
            ) {
                FavoriteCard(
                    message = m,
                    onOpen  = { onOpen(m.id, null) },
                    onStar = { onRemove(m.id) }
                )
            }
        }
    }
}

@Composable
private fun SwipeDismissItem(
    modifier: Modifier = Modifier,
    threshold: Dp = 96.dp,
    maxSwipe: Dp = 160.dp,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { threshold.toPx() }
    val maxPx = with(density) { maxSwipe.toPx() }

    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFD32F2F)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_delete),
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier
                    .padding(end = 20.dp)
                    .size(60.dp)
            )
        }

        Box(
            Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val next = (offsetX.value + dragAmount)
                                .coerceIn(-maxPx, 0f)
                            scope.launch { offsetX.snapTo(next) }
                        },
                        onDragEnd = {
                            val shouldDismiss = kotlin.math.abs(offsetX.value) > thresholdPx
                            scope.launch {
                                if (shouldDismiss) {
                                    offsetX.animateTo(
                                        targetValue = -maxPx,
                                        animationSpec = tween(durationMillis = 150)
                                    )
                                    onDismiss()
                                } else {
                                    offsetX.animateTo(0f, tween(180))
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, tween(180)) }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

@Composable
private fun FavoriteCard(
    message: Message,
    onOpen: () -> Unit,
    onStar: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HomePalette.CardBg)
            .border(1.dp, HomePalette.CardStroke, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
            .padding(14.dp)
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                message.category,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = HomePalette.TextTitle,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            val imageRes = when (message.type) {
                MessageType.TASK,
                MessageType.PHRASE -> Res.drawable.dog_card_image
                MessageType.BREATHING -> Res.drawable.breathing_card_image
            }

            Image(
                painter = painterResource(imageRes),
                contentDescription = "Favorite illustration",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(8.dp))

            val typeLabel = when (message.type) {
                MessageType.TASK      -> "Task"
                MessageType.BREATHING -> "Breathing"
                MessageType.PHRASE    -> "Phrase"
            }

            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(HomePalette.TagPill)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    typeLabel,
                    color = HomePalette.TagText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onStar),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Remove from favorites",
                tint = Color(0xFFFF9800)
            )
        }
    }
}