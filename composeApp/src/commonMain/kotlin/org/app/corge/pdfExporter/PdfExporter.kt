package org.app.corge.pdfExporter

import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.app.corge.data.repository.CorgeRepository

data class ExportCategoryShare(
    val label: String,
    val count: Int
)

data class ExportPayload(
    val generatedAt: String,
    val periodDays: Int,
    val streak: Int,
    val totalDaysDone: Int,
    val avgDurationSec: Int,
    val shares: List<ExportCategoryShare>
)

sealed class ExportResult {
    data class Ok(val location: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

expect class PdfExporter() {
    suspend fun export(payload: ExportPayload, fileName: String = defaultFileName()): ExportResult
}

interface ExportViewer {
    fun view(location: String)
}

expect fun exportFolderHint(): String

fun defaultFileName(): String {
    val now = kotlinx.datetime.Clock.System.now()
    val local = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val y = local.date.year
    val m = local.date.monthNumber.toString().padStart(2, '0')
    val d = local.date.dayOfMonth.toString().padStart(2, '0')
    return "Stats_${y}${m}${d}.pdf"
}

class BuildExportPayloadUseCase(private val repo: CorgeRepository) {

    suspend operator fun invoke(periodDays: Int): ExportPayload {
        val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
        val today = kotlinx.datetime.Clock.System.todayIn(tz)
        val start = today.minus(periodDays - 1, kotlinx.datetime.DateTimeUnit.DAY)

        val sessions = repo.getRecentSessions(400).filter { s ->
            val d = kotlinx.datetime.LocalDate.parse(s.date)
            d >= start && d <= today
        }

        val doneByDate = sessions.asSequence().filter { it.done }
            .map { kotlinx.datetime.LocalDate.parse(it.date) }.toSet()

        var cur = today
        var streak = 0
        while (doneByDate.contains(cur)) { streak++; cur = cur.minus(1, kotlinx.datetime.DateTimeUnit.DAY) }

        val avg = sessions.filter { it.done }.map { it.durationSeconds }.let {
            if (it.isEmpty()) 0 else it.sum() / it.size
        }

        val doneIds = sessions.asSequence().filter { it.done }.map { it.messageId }.toSet()
        val idToCategory = mutableMapOf<Long, String>()
        for (id in doneIds) idToCategory[id] = (repo.getMessageById(id)?.category ?: "Other")
        val shareMap = sessions.asSequence().filter { it.done }
            .map { idToCategory[it.messageId] ?: "Other" }
            .groupingBy { it }
            .eachCount()

        val shares = shareMap.entries.sortedByDescending { it.value }
            .map { ExportCategoryShare(label = it.key, count = it.value) }

        val generatedAt = kotlinx.datetime.Clock.System.now()
            .toLocalDateTime(tz).toString().replace('T', ' ')

        return ExportPayload(
            generatedAt = generatedAt,
            periodDays = periodDays,
            streak = streak,
            totalDaysDone = doneByDate.size,
            avgDurationSec = avg,
            shares = shares
        )
    }
}
