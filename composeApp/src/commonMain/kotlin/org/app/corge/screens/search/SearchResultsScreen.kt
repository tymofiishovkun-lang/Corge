package org.app.corge.screens.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import corge.composeapp.generated.resources.ic_done
import corge.composeapp.generated.resources.ic_not_done
import org.app.corge.data.model.Message
import org.app.corge.data.model.MessageType
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.screens.home.HomeDimens
import org.app.corge.screens.home.HomePalette
import org.app.corge.screens.settings.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    vm: SearchViewModel,
    onBack: () -> Unit,
    onOpenDetails: (Long, String?) -> Unit,
    themeRepo: ThemeRepository = koinInject()
) {
    val ui = vm.state

    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT

    val bgPainter = when (appTheme) {
        AppTheme.LIGHT    -> painterResource(Res.drawable.bg_settings_light)
        AppTheme.WABI     -> painterResource(Res.drawable.bg_settings_wabi)
        AppTheme.KINTSUGI -> painterResource(Res.drawable.bg_settings_kintsugi)
    }

    val sectionTitleColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextBody
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
            contentDescription = "Search Results background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { vm.toFilters(); onBack() },
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
                        val cat = ui.selectedCategoryIds.firstOrNull()
                            ?.let { id -> ui.categories.firstOrNull { it.id == id }?.title } ?: ""
                        val type = ui.selectedType?.label() ?: ""
                        Text(
                            listOf(cat, type, "Results")
                                .filter { it.isNotBlank() }
                                .joinToString("  "),
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
            ResultsList(
                messages = ui.results,
                doneIds = ui.doneIds,
                onItem = { id ->
                    onOpenDetails(id, ui.lastDateByMessageId[id]?.ifBlank { null })
                },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ResultsList(
    messages: List<Message>,
    onItem: (Long) -> Unit,
    modifier: Modifier = Modifier,
    doneIds: Set<Long> = emptySet()
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(messages, key = { it.id }) { msg ->
            val isDone = doneIds.contains(msg.id)
            ResultCardLarge(msg = msg, isDone = isDone, onClick = { onItem(msg.id) })
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun ResultCardLarge(
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
                contentDescription = "Result illustration",
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

