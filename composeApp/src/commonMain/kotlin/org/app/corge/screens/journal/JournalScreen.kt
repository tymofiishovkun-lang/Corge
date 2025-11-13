package org.app.corge.screens.journal

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import corge.composeapp.generated.resources.ic_delete1
import corge.composeapp.generated.resources.ic_done
import corge.composeapp.generated.resources.ic_edit
import corge.composeapp.generated.resources.ic_not_done
import org.app.corge.data.model.Category
import org.app.corge.data.model.MessageType
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.screens.home.HomeDimens
import org.app.corge.screens.home.HomePalette
import org.app.corge.screens.search.PillStyle
import org.app.corge.screens.search.SearchTheme
import org.app.corge.screens.search.SelectablePill
import org.app.corge.screens.search.TypeFilter
import org.app.corge.screens.settings.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onBack: () -> Unit,
    onOpenDetails: (Long, String) -> Unit,
    vm: JournalViewModel = koinInject(),
    themeRepo: ThemeRepository = koinInject()
) {
    LaunchedEffect(Unit) { vm.load() }
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
            contentDescription = "Journal background",
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
                            "Journal",
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
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                FilterBlock(
                    type = ui.type,
                    onType = vm::toggleType,
                    categories = ui.categories,
                    selectedCats = ui.selectedCats,
                    onToggleCat = vm::toggleCategory
                )

                Spacer(Modifier.height(8.dp))

                when {
                    ui.loading -> Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.04f))
                    )
                    ui.groups.isEmpty() -> EmptyJournal(appTheme = appTheme)
                    else -> JournalList(
                        groups = ui.groups,
                        onOpenDetails = onOpenDetails,
                        onStartEdit = vm::startEdit,
                        onEditDraft = vm::editDraft,
                        onCancelEdit = vm::cancelEdit,
                        onSaveNote = vm::saveNote,
                        onDeleteNote = vm::askDeleteNote,
                        onToggleDone = vm::toggleDone
                    )
                }
            }

            if (ui.showConfirmUnsetDone != null) {
                AlertDialog(
                    onDismissRequest = vm::dismissUnsetDone,
                    title = { Text("Do you want to remove the Done mark?") },
                    confirmButton = {
                        Button(
                            onClick = vm::confirmUnsetDone,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HomePalette.Primary)
                        ) { Text("Yes", color = Color.White) }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = vm::dismissUnsetDone,
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("No", color = HomePalette.TextTitle) }
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }

            if (ui.showDeleteNoteFor != null) {
                AlertDialog(
                    onDismissRequest = vm::dismissDeleteNote,
                    title = { Text("Delete this note?") },
                    confirmButton = {
                        Button(
                            onClick = vm::deleteNoteConfirmed,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HomePalette.Primary)
                        ) { Text("Yes", color = Color.White) }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = vm::dismissDeleteNote,
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("No", color = HomePalette.TextTitle) }
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterBlock(
    type: TypeFilter?,
    onType: (TypeFilter) -> Unit,
    categories: List<Category>,
    selectedCats: Set<String>,
    onToggleCat: (String) -> Unit,
    themeRepo: ThemeRepository = koinInject()
) {
    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT

    val sectionTitleColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextBody
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
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
            listOf(TypeFilter.Phrase, TypeFilter.Breathing, TypeFilter.Task).forEach { t ->
                val selected = type == t
                val label = when (t) {
                    TypeFilter.Phrase     -> "Phrase"
                    TypeFilter.Breathing  -> "Breathing practice"
                    TypeFilter.Task       -> "Task"
                }

                SelectablePill(
                    label = label,
                    selected = selected,
                    onClick = { onType(t) },
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
            categories.forEach { c ->
                val selected = selectedCats.contains(c.id)

                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selected) SearchTheme.Primary else SearchTheme.BgChip
                        )
                        .clickable { onToggleCat(c.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = c.title,
                        color = if (selected) Color.White else SearchTheme.Body,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalList(
    groups: Map<String, List<JournalViewModel.Item>>,
    onOpenDetails: (Long, String) -> Unit,
    onStartEdit: (String, Long) -> Unit,
    onEditDraft: (String, Long, String) -> Unit,
    onCancelEdit: (String, Long) -> Unit,
    onSaveNote: (String, Long) -> Unit,
    onDeleteNote: (String, Long) -> Unit,
    onToggleDone: (String, Long) -> Unit,
    themeRepo: ThemeRepository = koinInject()
) {
    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT

    val sectionTitleColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextBody
    }

    val dates = groups.keys.sortedDescending()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        dates.forEach { date ->
            item(key = "header-$date") {
                Text(
                    date,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = sectionTitleColor, fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            }
            items(groups[date]!!, key = { it.session.id }) { it ->
                JournalCard(
                    item = it,
                    onOpen = { onOpenDetails(it.message.id, date) },
                    onToggleDone = { onToggleDone(date, it.message.id) },
                    onStartEdit = { onStartEdit(date, it.message.id) },
                    onEditDraft = { s -> onEditDraft(date, it.message.id, s) },
                    onCancelEdit = { onCancelEdit(date, it.message.id) },
                    onSaveNote = { onSaveNote(date, it.message.id) },
                    onDeleteNote = { onDeleteNote(date, it.message.id) }
                )
            }
        }
    }
}

@Composable
private fun JournalCard(
    item: JournalViewModel.Item,
    onOpen: () -> Unit,
    onToggleDone: () -> Unit,
    onStartEdit: () -> Unit,
    onEditDraft: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveNote: () -> Unit,
    onDeleteNote: () -> Unit
) {
    val m = item.message
    val isDone = item.session.done

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HomeDimens.BigCardRadius))
            .background(HomePalette.CardBg)
            .border(1.dp, HomePalette.CardStroke, RoundedCornerShape(HomeDimens.BigCardRadius))
            .clickable(onClick = onOpen)
            .padding(HomeDimens.InnerPad)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = m.category,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = HomePalette.TextTitle,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = .2.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            val imageRes = when (m.type) {
                MessageType.TASK,
                MessageType.PHRASE -> Res.drawable.dog_card_image
                MessageType.BREATHING -> Res.drawable.breathing_card_image
            }

            Image(
                painter = painterResource(imageRes),
                contentDescription = "Journal illustration",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(8.dp))

            val tag = when (m.type) {
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
                    tag,
                    color = HomePalette.TagText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

            NoteInline(
                isEditing = item.isEditing,
                text = item.session.note.orEmpty(),
                draft = item.draft,
                onStartEdit = onStartEdit,
                onEditDraft = onEditDraft,
                onCancelEdit = onCancelEdit,
                onSave = onSaveNote,
                onDelete = onDeleteNote
            )
        }

        val badgeIcon = if (isDone)
            painterResource(Res.drawable.ic_done)
        else
            painterResource(Res.drawable.ic_not_done)

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(40.dp)
                .clickable { onToggleDone() },
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
private fun NoteInline(
    isEditing: Boolean,
    text: String,
    draft: String,
    onStartEdit: () -> Unit,
    onEditDraft: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    val radius = 12.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius))
            .background(Color(0xFFE4C8A7))
            .border(1.dp, HomePalette.ButtonStroke, RoundedCornerShape(radius))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        if (isEditing) {
            OutlinedTextField(
                value = draft,
                onValueChange = { if (it.length <= 280) onEditDraft(it) },
                textStyle = MaterialTheme.typography.bodySmall.copy(color = HomePalette.TextBody),
                minLines = 1,
                maxLines = 2,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HomePalette.TextTitle,
                    unfocusedBorderColor = HomePalette.TextTitle,
                    cursorColor = HomePalette.Primary,
                    focusedTextColor = HomePalette.TextBody,
                    unfocusedTextColor = HomePalette.TextBody,
                    focusedContainerColor = Color(0xFFE4C8A7),
                    unfocusedContainerColor = Color(0xFFE4C8A7)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp, max = 90.dp)
            )

            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onCancelEdit,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, HomePalette.CardStroke),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = Color.Black)
                }

                Button(
                    onClick = onSave,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomePalette.Primary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save", color = Color.Black)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (text.isBlank()) "—" else text,
                    style = MaterialTheme.typography.bodySmall.copy(color = HomePalette.TextBody),
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onStartEdit, modifier = Modifier.size(40.dp)) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_edit),
                            contentDescription = "Edit note",
                            tint = HomePalette.Primary,
                            modifier = Modifier.size(55.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_delete1),
                            contentDescription = "Delete note",
                            tint = Color(0xFFB64C4C),
                            modifier = Modifier.size(55.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyJournal(appTheme: AppTheme) {
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
            "Your journal is empty.",
            style = MaterialTheme.typography.titleMedium.copy(color = sectionTitleColor),
            textAlign = TextAlign.Center
        )
    }
}

