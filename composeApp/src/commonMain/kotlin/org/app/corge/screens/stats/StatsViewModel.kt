package org.app.corge.screens.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.app.corge.data.model.Message
import org.app.corge.data.repository.CorgeRepository
import kotlin.collections.filter

enum class StatsPeriod(val days: Int, val label: String) {
    D7(7, "7 days"),
    D30(30, "30 days"),
    D365(365, "365 days");
}

data class CategoryShare(
    val label: String,
    val color: Color,
    val count: Int
)

data class DayCell(
    val date: LocalDate,
    val isDone: Boolean
)

data class StatsUi(
    val loading: Boolean = true,
    val period: StatsPeriod = StatsPeriod.D30,
    val streak: Int = 0,
    val totalDaysDone: Int = 0,
    val avgDurationSec: Int = 0,
    val shares: List<CategoryShare> = emptyList(),
    val calendar: List<DayCell> = emptyList()
) {
    val isEmpty: Boolean get() = totalDaysDone == 0
}

class StatsViewModel(
    private val repo: CorgeRepository
) : ViewModel() {

    var ui by mutableStateOf(StatsUi())
        private set

    fun load(period: StatsPeriod = ui.period) {
        viewModelScope.launch {
            ui = ui.copy(
                loading = true,
                period = period,
                calendar = emptyList()
            )

            val tz = TimeZone.currentSystemDefault()
            val today: LocalDate = Clock.System.todayIn(tz)
            val start: LocalDate = today.minus(period.days - 1, DateTimeUnit.DAY)

            val sessions = withContext(Dispatchers.Default) {
                repo.getRecentSessions(400).filter { s ->
                    val d = LocalDate.parse(s.date)
                    d >= start && d <= today
                }
            }

            val doneByDate: Set<LocalDate> =
                sessions.asSequence().filter { it.done }.map { LocalDate.parse(it.date) }.toSet()
            val streak = calcStreak(doneByDate, today)

            val uniqueDoneDays = doneByDate.size
            val avg = sessions.filter { it.done }.map { it.durationSeconds }.let {
                if (it.isEmpty()) 0 else it.sum() / it.size
            }

            val doneIds: Set<Long> = sessions.asSequence().filter { it.done }.map { it.messageId }.toSet()
            val idToCategory: Map<Long, String> = withContext(Dispatchers.Default) {
                val map = mutableMapOf<Long, String>()
                for (id in doneIds) map[id] = repo.getMessageById(id)?.category ?: "Other"
                map
            }
            val shareMap: Map<String, Int> = sessions.asSequence()
                .filter { it.done }
                .map { s -> idToCategory[s.messageId] ?: "Other" }
                .groupingBy { it }
                .eachCount()
            val shares = shareMap.entries
                .sortedByDescending { it.value }
                .map { (cat, cnt) -> CategoryShare(cat, categoryColor(cat), cnt) }

            val days: List<DayCell> = when (period) {
                StatsPeriod.D7, StatsPeriod.D30 -> (0 until period.days).map { delta ->
                    val d = today.minus((period.days - 1 - delta), DateTimeUnit.DAY)
                    DayCell(date = d, isDone = d in doneByDate)
                }
                StatsPeriod.D365 -> emptyList()
            }

            ui = ui.copy(
                loading = false,
                streak = streak,
                totalDaysDone = uniqueDoneDays,
                avgDurationSec = avg,
                shares = shares,
                calendar = days
            )
        }
    }

    private fun calcStreak(doneDays: Set<LocalDate>, today: LocalDate): Int {
        var cur = today
        var streak = 0
        while (doneDays.contains(cur)) {
            streak++
            cur = cur.minus(1, DateTimeUnit.DAY)
        }
        return streak
    }

    private fun categoryColor(label: String): Color = when (label.lowercase()) {
        "calmness"       -> Color(0xFFFFA7C2)
        "awareness"      -> Color(0xFFF3C9B4)
        "purposefulness" -> Color(0xFF6E4B20)
        "gratitude"      -> Color(0xFFFF914D)
        "liberation"     -> Color(0xFFFFFFFF)
        "relaxation"     -> Color(0xFFF5BEA8)
        else             -> Color(0xFF8E7C6D)
    }
}