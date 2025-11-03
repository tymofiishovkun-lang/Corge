package org.app.corge.screens.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import corge.composeapp.generated.resources.bg_home_kintsugi
import corge.composeapp.generated.resources.bg_home_light
import corge.composeapp.generated.resources.bg_settings_kintsugi
import corge.composeapp.generated.resources.bg_settings_light
import corge.composeapp.generated.resources.bg_settings_wabi
import corge.composeapp.generated.resources.ic_checkbox_checked
import corge.composeapp.generated.resources.ic_checkbox_unchecked
import corge.composeapp.generated.resources.ic_delete
import corge.composeapp.generated.resources.ic_search
import org.app.corge.data.model.Message
import org.app.corge.data.model.MessageType
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.screens.bottomBar.BottomBar
import org.app.corge.screens.home.HomePalette
import org.app.corge.screens.settings.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

object SearchTheme {
    val BgChip = HomePalette.ChipBg
    val BgPill = HomePalette.TagPill
    val TextPill = HomePalette.TagText
    val Primary = HomePalette.Primary
    val Title = HomePalette.TextTitle
    val Body = HomePalette.TextBody
    val FieldBg = Color(0xFFF6EDDF)
    val FieldStroke = Color(0xFFE9D8C3)
    val Indicator = Color(0xFFEAD8C7)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit = {},
    onOpenDetails: (Long) -> Unit = {},
    onOpenResults: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: SearchViewModel,
    themeRepo: ThemeRepository = koinInject()
) {
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

    LaunchedEffect(Unit) { viewModel.load() }
    val ui = viewModel.state

    LaunchedEffect(ui.mode) {
        if (ui.mode == SearchMode.Results) onOpenResults()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = bgPainter,
            contentDescription = "Search background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Search",
                            color = sectionTitleColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                BottomBar(
                    selected = 0,
                    onExplore  = { },
                    onHome     = onOpenHome,
                    onSettings = onOpenSettings
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            when (ui.mode) {
                SearchMode.Filters -> FiltersContent(
                    vm = viewModel,
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                )

                SearchMode.Empty -> EmptyResults(
                    onReset = { viewModel.resetFiltersFromEmpty() },
                    modifier = Modifier.padding(padding),
                    appTheme = appTheme
                )

                SearchMode.Results -> Unit
            }
        }
    }
}

@Composable
private fun FiltersContent(
    vm: SearchViewModel,
    modifier: Modifier = Modifier,
    themeRepo: ThemeRepository = koinInject()
) {
    val ui = vm.state
    val scroll = rememberScrollState()

    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT

    val sectionTitleColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextBody
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SearchField(ui.query, vm::onQueryChange, modifier = Modifier.fillMaxWidth())

        Text(
            "Type",
            color = sectionTitleColor,
            fontWeight = FontWeight.SemiBold
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TypeFilter.entries.forEach { t ->
                val selected = ui.selectedType == t
                SelectablePill(
                    label = t.label(),
                    selected = selected,
                    onClick = { vm.toggleType(t) },
                    selectedStyle = PillStyle.Primary
                )
            }
        }

        Text(
            "Categories",
            color = sectionTitleColor,
            fontWeight = FontWeight.SemiBold
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ui.categories.forEach { cat ->
                val key = cat.title
                val selected = ui.selectedCategoryIds.contains(key)

                Box(
                    modifier = Modifier
                        .width(104.dp)
                        .height(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        SelectablePill(
                            label = cat.title,
                            selected = selected,
                            onClick = { vm.toggleCategory(key) },
                            selectedStyle = PillStyle.Tag
                        )
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SquareCheckRow(
                checked = ui.onlyUnread,
                onCheckedChange = vm::toggleUnread,
                label = "Show only unread"
            )
            SquareCheckRow(
                checked = ui.onlyFavorites,
                onCheckedChange = vm::toggleFavs,
                label = "Show only favorites"
            )
        }

        if (ui.recentQueries.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent searches",
                    color = sectionTitleColor,
                    fontWeight = FontWeight.SemiBold
                )

                Image(
                    painter = painterResource(Res.drawable.ic_delete),
                    contentDescription = "Clear recent searches",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { vm.clearRecents() }
                        .padding(2.dp)
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ui.recentQueries.forEach { q ->
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(17.dp))
                            .background(SearchTheme.BgPill)
                            .clickable {
                                vm.useRecentQuery(q)
                                vm.applyFilters()
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            q,
                            color = SearchTheme.TextPill,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton("Apply Filters", onClick = vm::applyFilters, modifier = Modifier.weight(1f))
            OutlineButton("Clear", onClick = vm::clearAll, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun EmptyResults(
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
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
        modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = bgPainter,
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(8.dp))
        Text("No messages found.", color = sectionTitleColor)
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onReset,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.6f),
                disabledContentColor = Color.Black.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f)),
            modifier = Modifier.height(44.dp)
        ) {
            Text("Reset filters", color = Color.Black, fontWeight = FontWeight.SemiBold)
        }
    }
}


@Composable
fun ResultsGrid(
    ui: SearchUiState,
    onSortChange: (SortMode) -> Unit,
    onItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val list = remember(ui.results, ui.sort) {
        when (ui.sort) {
            SortMode.Recommended -> ui.results
            SortMode.AZ -> ui.results.sortedBy { it.category + " " + it.textEn }
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            SegmentedButtons(sort = ui.sort, onSortChange = onSortChange)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(list, key = { it.id }) { msg ->
                MediumCard(msg = msg, onClick = { onItem(msg.id) })
            }
        }
    }
}

@Composable
private fun SegmentedButtons(
    sort: SortMode,
    onSortChange: (SortMode) -> Unit
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(HomePalette.ChipBg)
            .padding(4.dp)
    ) {
        @Composable
        fun tab(label: String, selected: Boolean, onClick: () -> Unit) {
            val bg = if (selected) HomePalette.Primary else Color.Transparent
            val fg = if (selected) Color.White else HomePalette.TextBody
            TextButton(
                onClick = onClick,
                colors = ButtonDefaults.textButtonColors(containerColor = bg, contentColor = fg),
                shape = RoundedCornerShape(16.dp)
            ) { Text(label) }
        }
        tab("Recommended", sort == SortMode.Recommended) { onSortChange(SortMode.Recommended) }
        tab("A–Z",         sort == SortMode.AZ)           { onSortChange(SortMode.AZ) }
    }
}

@Composable
private fun MediumCard(
    msg: Message,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HomePalette.CardBg)
            .border(1.dp, HomePalette.CardStroke, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(
            text = msg.category,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(
                color = HomePalette.TextTitle,
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("🐶") }
        Spacer(Modifier.height(6.dp))

        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(16.dp))
                .background(HomePalette.TagPill)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            val ty = when (msg.type) {
                MessageType.TASK -> "Task"
                MessageType.BREATHING -> "Breathing"
                MessageType.PHRASE -> "Phrase"
            }
            Text(
                ty,
                fontSize = 12.sp,
                color = HomePalette.TagText,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = msg.textEn,
            maxLines = 3,
            style = MaterialTheme.typography.bodySmall.copy(color = HomePalette.TextBody)
        )
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = { Text("Search", color = SearchTheme.Body.copy(.55f)) },

        leadingIcon = {
            Image(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = "Search icon",
                modifier = Modifier.size(22.dp)
            )
        },

        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SearchTheme.FieldBg,
            unfocusedContainerColor = SearchTheme.FieldBg,
            focusedBorderColor = SearchTheme.FieldStroke,
            unfocusedBorderColor = SearchTheme.FieldStroke,
            cursorColor = SearchTheme.Title
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    )
}

@Composable
fun SelectablePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedStyle: PillStyle = PillStyle.Primary
) {
    val (bg, fg) = when {
        selected && selectedStyle == PillStyle.Primary -> SearchTheme.Primary to Color.White
        selected && selectedStyle == PillStyle.Tag     -> SearchTheme.Primary to Color.White
        else -> SearchTheme.BgChip to SearchTheme.Body
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(17.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) { Text(label, color = fg, fontWeight = FontWeight.SemiBold) }
}

enum class PillStyle { Primary, Tag }

@Composable
private fun SquareCheckRow(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    label: String,
    themeRepo: ThemeRepository = koinInject()
) {
    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT

    val sectionTitleColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextBody
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        AnimatedContent(targetState = checked, label = "checkbox") { isChecked ->
            val icon = if (isChecked) Res.drawable.ic_checkbox_checked else Res.drawable.ic_checkbox_unchecked
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .size(26.dp)
                    .clickable(onClick = onCheckedChange)
            )
        }

        Text(
            text = label,
            color = sectionTitleColor,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SearchTheme.Primary
        ),
        modifier = modifier.height(44.dp)
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OutlineButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.White.copy(alpha = 0.6f),
            disabledContentColor = Color.Black.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f)),
        modifier = modifier.height(44.dp)
    ) {
        Text(label, color = Color.Black, fontWeight = FontWeight.SemiBold)
    }
}

