package org.app.corge.screens.stats

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import corge.composeapp.generated.resources.Res
import corge.composeapp.generated.resources.bg_home_kintsugi
import corge.composeapp.generated.resources.bg_home_light
import corge.composeapp.generated.resources.bg_settings_kintsugi
import corge.composeapp.generated.resources.bg_settings_light
import corge.composeapp.generated.resources.bg_settings_wabi
import corge.composeapp.generated.resources.ic_back
import corge.composeapp.generated.resources.ic_back_kintsugi
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.app.corge.data.repository.ThemeRepository
import org.app.corge.screens.home.HomePalette
import org.app.corge.screens.settings.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    vm: StatsViewModel = koinViewModel(),
    themeRepo: ThemeRepository = koinInject()
) {
    LaunchedEffect(Unit) { vm.load(StatsPeriod.D30) }
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
            contentDescription = "Stats background",
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
                            "Stats",
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                PeriodSwitch(current = ui.period, onSelect = { vm.load(it) })

                Spacer(Modifier.height(20.dp))

                Crossfade(
                    modifier = Modifier.fillMaxSize(),
                    targetState = ui.loading to ui.isEmpty,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) { (loading, empty) ->
                    when {
                        loading -> Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.04f))
                        )
                        empty -> EmptyStats(appTheme = appTheme)
                        else -> StatsContent(
                            ui = ui,
                            onPeriod = { vm.load(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsContent(
    ui: StatsUi,
    onPeriod: (StatsPeriod) -> Unit,
    themeRepo: ThemeRepository = koinInject()
) {
    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        when (ui.period) {
            StatsPeriod.D7, StatsPeriod.D30 -> CalendarCard(days = ui.calendar)
            StatsPeriod.D365 -> YearGrid(startYear = 2025, endYear = 2030)
        }

        val donutKey = ui.period to ui.shares.hashCode()
        DonutCard(shares = ui.shares, animateKey = donutKey, appTheme = appTheme)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Streak",
                value = ui.streak.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Days Done",
                value = ui.totalDaysDone.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        MetricCard(
            title = "Avg",
            value = formatDuration(ui.avgDurationSec),
            modifier = Modifier.fillMaxWidth(.5f)
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun YearGrid(
    startYear: Int,
    endYear: Int,
    modifier: Modifier = Modifier,
    themeRepo: ThemeRepository = koinInject()
) {
    val tz = TimeZone.currentSystemDefault()
    val currentYear = remember { Clock.System.todayIn(tz).year }

    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT

    val sectionTitleColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextTitle
    }

    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            "Years",
            style = MaterialTheme.typography.labelLarge.copy(color = sectionTitleColor)
        )

        Spacer(Modifier.height(10.dp))

        val years = (startYear..endYear).toList()
        val columns = 3
        val rows = (years.size + columns - 1) / columns

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(rows) { r ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(columns) { c ->
                        val idx = r * columns + c
                        if (idx < years.size) {
                            val y = years[idx]
                            YearChip(
                                year = y,
                                selected = y == currentYear,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearChip(
    year: Int,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val activeBg = Color(0xFFE4C8A7)
    val inactiveBg = Color.White
    val textColor = Color(0xFFFF8727)
    val stroke = if (selected) Color(0xFFE78E56) else HomePalette.CardStroke

    Box(
        modifier
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) activeBg else inactiveBg)
            .border(2.dp, stroke, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = year.toString(),
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PeriodSwitch(
    current: StatsPeriod,
    onSelect: (StatsPeriod) -> Unit,
    themeRepo: ThemeRepository = koinInject()
) {
    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT

    val sectionTitleColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextTitle
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Period",
            style = MaterialTheme.typography.labelLarge.copy(color = sectionTitleColor)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatsPeriod.values().forEach { p ->
                val selected = current == p

                val bgColor = if (selected) Color(0xFFFF8727) else HomePalette.ChipBg
                val textColor = Color.Black

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(bgColor)
                        .clickable { onSelect(p) }
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = p.label,
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarCard(days: List<DayCell>, themeRepo: ThemeRepository = koinInject()) {

    val currentThemeId by themeRepo.currentThemeId.collectAsState(initial = AppTheme.LIGHT.id)
    val appTheme = AppTheme.entries.firstOrNull { it.id == currentThemeId } ?: AppTheme.LIGHT

    val sectionTitleColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color(0xFFF9F1D4)
        else              -> HomePalette.TextTitle
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        val week = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            week.forEach { w ->
                Text(
                    w,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(color = sectionTitleColor)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        val cellSize = 34.dp
        val columns = 7
        val rows = (days.size + columns - 1) / columns

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(rows) { r ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(columns) { c ->
                        val idx = r * columns + c
                        if (idx < days.size) {
                            val cell = days[idx]
                            CalendarCell(cell = cell, size = cellSize)
                        } else {
                            Spacer(Modifier.width(cellSize))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCell(cell: DayCell, size: Dp) {
    val activeBg = Color(0xFFE4C8A7)
    val inactiveBg = Color.White
    val textColor = Color(0xFFFF8727)
    val stroke = if (cell.isDone) Color(0xFFE78E56) else HomePalette.CardStroke

    val scale by animateFloatAsState(
        targetValue = if (cell.isDone) 1f else .98f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cellScale"
    )

    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(if (cell.isDone) activeBg else inactiveBg)
            .border(2.dp, stroke, RoundedCornerShape(8.dp))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cell.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium.copy(
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun DonutCard(
    shares: List<CategoryShare>,
    animateKey: Any?,
    appTheme: AppTheme
) {
    val backgroundColor = when (appTheme) {
        AppTheme.LIGHT,
        AppTheme.WABI -> Color(0xFFE5D6C0).copy(alpha = 0.45f)
        AppTheme.KINTSUGI -> Color(0xFFB57F54)
    }

    val borderColor = when (appTheme) {
        AppTheme.KINTSUGI -> HomePalette.CardStroke.copy(alpha = 0.6f)
        else -> HomePalette.CardStroke.copy(alpha = 0.4f)
    }

    val textColor = when (appTheme) {
        AppTheme.KINTSUGI -> Color.Black
        else -> HomePalette.TextBody
    }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DonutChart(
                segments = shares.map { it.color to it.count },
                animateKey = animateKey,
                modifier = Modifier.size(160.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                shares.forEach { s ->
                    LegendRow(
                        color = s.color,
                        label = s.label,
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendRow(
    color: Color,
    label: String,
    textColor: Color = HomePalette.TextBody
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
        )
    }
}

@Composable
private fun DonutChart(
    segments: List<Pair<Color, Int>>,
    modifier: Modifier = Modifier,
    thicknessFraction: Float = 0.24f,
    borderFraction: Float = 0.065f,
    gapDeg: Float = 3.2f,
    animateKey: Any? = null
) {
    val total = segments.sumOf { it.second }.coerceAtLeast(1)

    val global = remember(segments, animateKey) { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(segments, animateKey) {
        global.snapTo(0f)
        global.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing)
        )
    }

    val fractions = remember(segments, total) {
        val sum = total.toFloat().coerceAtLeast(1f)
        val bounds = mutableListOf<Pair<Float, Float>>()
        var acc = 0f
        segments.forEach { (_, v) ->
            val p = v / sum
            bounds += acc to (acc + p).coerceAtMost(1f)
            acc += p
        }
        bounds
    }

    Canvas(modifier) {
        val d = size.minDimension
        val ringW   = d * thicknessFraction
        val borderW = ringW * borderFraction
        val segW    = ringW - borderW * 2f

        val trackStroke = Stroke(width = ringW,   cap = StrokeCap.Round)
        val segStroke   = Stroke(width = segW,    cap = StrokeCap.Butt)

        val inset = ringW / 2f
        val arcSize    = Size(d - inset * 2, d - inset * 2)
        val arcTopLeft = Offset(inset, inset)

        drawArc(
            color = Color(0x66000000),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = ringW + 2f, cap = StrokeCap.Round),
            size = arcSize,
            topLeft = arcTopLeft
        )

        drawArc(
            color = Color.White,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = trackStroke,
            size = arcSize,
            topLeft = arcTopLeft
        )

        var startDeg = -90f
        segments.forEachIndexed { i, (color, value) ->
            val sweepRaw = 360f * (value.toFloat() / total.toFloat())
            val (startFrac, endFrac) = fractions[i]
            val local = ((global.value - startFrac) / (endFrac - startFrac).coerceAtLeast(1e-6f))
                .coerceIn(0f, 1f)
            val animatedSweep = (sweepRaw - gapDeg).coerceAtLeast(0f) * local

            if (animatedSweep > 0.5f) {
                drawArc(
                    color = color,
                    startAngle = startDeg + gapDeg / 2f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = segStroke,
                    size = arcSize,
                    topLeft = arcTopLeft
                )
            }
            startDeg += sweepRaw
        }

        val innerRadius = d * (0.5f - thicknessFraction) + borderW * 0.5f
        drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            style = Stroke(width = borderW),
            radius = innerRadius,
            center = center
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFA24D))
            .border(1.dp, HomePalette.CardStroke, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(title, color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

private fun formatDuration(sec: Int): String {
    if (sec <= 0) return "–"
    val h = sec / 3600
    val m = (sec % 3600) / 60
    val s = sec % 60
    return if (h > 0) {
        "${h}:${m.toString().padStart(2,'0')}:${s.toString().padStart(2,'0')}"
    } else {
        "${m}:${s.toString().padStart(2,'0')}"
    }
}

@Composable
private fun EmptyStats(
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
            .padding(horizontal = 16.dp),
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
            "You have no data yet.",
            style = MaterialTheme.typography.titleMedium.copy(
                color = sectionTitleColor
            ),
            textAlign = TextAlign.Center
        )
    }
}

