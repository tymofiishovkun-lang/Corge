package org.app.corge.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.app.corge.data.Corge
import org.app.corge.data.content.BundledContent
import org.app.corge.data.mappers.toModel
import org.app.corge.data.model.Category
import org.app.corge.data.model.Message
import org.app.corge.data.model.Session
import org.app.corge.data.model.Stats
import org.app.corge.data.model.Theme

interface CorgeRepository {

    suspend fun getAllMessages(): List<Message>
    suspend fun getMessageById(id: Long): Message?
    suspend fun getMessagesByCategory(category: String): List<Message>
    suspend fun searchMessages(query: String): List<Message>

    suspend fun insertSession(
        messageId: Long,
        note: String? = null,
        done: Boolean = true,
        duration: Int = 0,
        date: String? = null
    )

    suspend fun getSessionByDate(date: String): Session?
    suspend fun getRecentSessions(limit: Int = 10): List<Session>

    suspend fun toggleFavorite(messageId: Long)
    suspend fun getFavorites(): List<Message>

    suspend fun getAllCategories(): List<Category>

    suspend fun getAllThemes(): List<Theme>
    suspend fun markThemePurchased(id: String)
    suspend fun getStats(): Stats
    suspend fun prepopulateIfNeeded()
    suspend fun getDoneMessageIds(): Set<Long>
    suspend fun setSessionDoneByDate( messageId: Long, date: String, done: Boolean)
    fun todaySessionFlow(date: String): Flow<Session?>
    fun doneIdsFlow(): Flow<Set<Long>>
    suspend fun getSessionByMessageAndDate(messageId: Long, date: String): Session?
    suspend fun setSessionDoneByMessageAndDate(messageId: Long, date: String, done: Boolean)
    fun sessionFlow(messageId: Long, date: String): Flow<Session?>
    fun favoritesFlow(): Flow<List<Message>>
    fun favoriteIdsFlow(): Flow<Set<Long>>
    suspend fun updateSessionByMessageAndDate(
        messageId: Long,
        date: String,
        done: Boolean,
        duration: Int,
        note: String?
    )
    suspend fun getLastSessionForMessage(messageId: Long): Session?
    suspend fun resetProgress()
}

class CorgeRepositoryImpl(
    private val db: Corge
) : CorgeRepository {

    private val corgeQueries = db.corgeQueries

    override suspend fun getAllMessages(): List<Message> = withContext(Dispatchers.Default) {
        corgeQueries.selectAllMessage().executeAsList().map { it.toModel() }
    }

    override suspend fun getMessageById(id: Long): Message? = withContext(Dispatchers.Default) {
        corgeQueries.selectById(id).executeAsOneOrNull()?.toModel()
    }

    override suspend fun getMessagesByCategory(category: String): List<Message> = withContext(Dispatchers.Default) {
        corgeQueries.selectByCategory(category).executeAsList().map { it.toModel() }
    }

    override suspend fun searchMessages(query: String): List<Message> = withContext(Dispatchers.Default) {
        corgeQueries.searchMessages(query).executeAsList().map { it.toModel() }
    }

    override suspend fun insertSession(
        messageId: Long,
        note: String?,
        done: Boolean,
        duration: Int,
        date: String?
    ) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dateStr = date ?: "${now.date}"

        val existing = corgeQueries
            .selectByMessageAndDate(messageId, dateStr)
            .executeAsOneOrNull()

        if (existing == null) {
            corgeQueries.insertSession(
                message_id = messageId,
                date = dateStr,
                started_at = now.toString(),
                duration_seconds = duration.toLong(),
                done = done,
                note = note
            )
        } else {
            val newDone = (existing.done == true) || done
            val newNote = note ?: existing.note
            val newDuration = if (duration > 0) duration.toLong()
            else (existing.duration_seconds ?: 0L)

            corgeQueries.updateSessionByMessageAndDate(
                done = newDone,
                note = newNote,
                duration_seconds = newDuration,
                message_id = messageId,
                date = dateStr
            )
        }
    }

    override suspend fun getSessionByDate(date: String): Session? = withContext(Dispatchers.Default) {
        corgeQueries.selectByDate(date).executeAsOneOrNull()?.toModel()
    }

    override suspend fun getRecentSessions(limit: Int): List<Session> = withContext(Dispatchers.Default) {
        corgeQueries.selectRecent(limit.toLong()).executeAsList().map { it.toModel() }
    }

    override suspend fun toggleFavorite(messageId: Long) {
        val existing = corgeQueries.selectByMessageId(messageId).executeAsOneOrNull()
        if (existing != null) {
            corgeQueries.deleteByMessageId(messageId)
        } else {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            corgeQueries.insertFavorite(messageId, now.toString())
        }
    }

    override suspend fun getFavorites(): List<Message> = withContext(Dispatchers.Default) {
        corgeQueries.selectAllFavorite().executeAsList().map { it.toModel() }
    }

    override suspend fun getAllCategories(): List<Category> = withContext(Dispatchers.Default) {
        corgeQueries.selectAllCategory().executeAsList().map { it.toModel() }
    }

    override suspend fun getAllThemes(): List<Theme> = withContext(Dispatchers.Default) {
        corgeQueries.selectAllTheme().executeAsList().map { it.toModel() }
    }

    override suspend fun markThemePurchased(id: String) = withContext(Dispatchers.Default) {
        corgeQueries.updatePurchased(id)
        Unit
    }

    override suspend fun getStats(): Stats = withContext(Dispatchers.Default) {
        val sessions = corgeQueries.selectAllSession().executeAsList().map { it.toModel() }
        val totalDone = sessions.count { it.done }
        val avgDuration = if (sessions.isNotEmpty()) sessions.map { it.durationSeconds }.average() else 0.0
        val streak = calculateStreak(sessions)
        Stats(totalDone, avgDuration.toInt(), streak)
    }

    private fun calculateStreak(sessions: List<Session>): Int {
        val dates = sessions.filter { it.done }.map { it.date }.distinct().sortedDescending()
        if (dates.isEmpty()) return 0
        var streak = 1
        for (i in 1 until dates.size) {
            val prev = LocalDate.parse(dates[i - 1])
            val curr = LocalDate.parse(dates[i])
            if (prev == curr.plus(1, DateTimeUnit.DAY)) streak++
            else break
        }
        return streak
    }

    override suspend fun prepopulateIfNeeded() = withContext(Dispatchers.Default) {
        if (corgeQueries.selectAllMessage().executeAsList().isNotEmpty()) return@withContext

        corgeQueries.transaction {
            BundledContent.categories.forEach { c ->
                corgeQueries.insertCategory(c.id, c.title, c.description)
            }
            BundledContent.messages.forEachIndexed { idx, m ->
                corgeQueries.insertMessage(
                    order_index = (idx + 1).toLong(),
                    type = m.type,
                    category = m.category,
                    text_en = m.text,
                    illustration_name = m.illustrationName,
                    ritual = m.ritual,
                    why_it_matters = m.whyItMatters,
                    recommended_time = m.recommendedTime,
                    duration_seconds = m.durationSeconds?.toLong(),
                    breathing_related = m.breathingRelated
                )
            }
        }
    }

    override suspend fun getDoneMessageIds(): Set<Long> = withContext(Dispatchers.Default) {
        db.corgeQueries.selectAllSession()
            .executeAsList()
            .asSequence()
            .filter { it.done == true }
            .map { it.message_id }
            .toSet()
    }

    override suspend fun setSessionDoneByDate(
        messageId: Long,
        date: String,
        done: Boolean
    ) = withContext(Dispatchers.Default) {
        db.corgeQueries.updateDoneByMessageAndDate(done, messageId, date)
        Unit
    }

    override fun todaySessionFlow(date: String): Flow<Session?> =
        corgeQueries.selectByDate(date)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toModel() }

    override fun doneIdsFlow(): Flow<Set<Long>> =
        corgeQueries.selectAllSession()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows ->
                rows.asSequence()
                    .filter { it.done == true }
                    .map { it.message_id }
                    .toSet()
            }
    override suspend fun getSessionByMessageAndDate(messageId: Long, date: String): Session? =
        withContext(Dispatchers.Default) {
            corgeQueries.selectByMessageAndDate(messageId, date).executeAsOneOrNull()?.toModel()
        }

    override suspend fun setSessionDoneByMessageAndDate(messageId: Long, date: String, done: Boolean) =
        withContext(Dispatchers.Default) {
            corgeQueries.updateDoneByMessageAndDate(done, messageId, date)
            Unit
        }

    override fun sessionFlow(messageId: Long, date: String): Flow<Session?> =
        corgeQueries
            .selectByMessageAndDate(messageId, date)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toModel() }

    override fun favoritesFlow(): Flow<List<Message>> =
        corgeQueries.selectAllFavorite()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toModel() } }

    override fun favoriteIdsFlow(): Flow<Set<Long>> =
        corgeQueries.selectAllFavorite()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.id }.toSet() }

    override suspend fun updateSessionByMessageAndDate(
        messageId: Long,
        date: String,
        done: Boolean,
        duration: Int,
        note: String?
    ) = withContext(Dispatchers.Default) {
        db.corgeQueries.updateSessionByMessageAndDate(
            done = done,
            note = note,
            duration_seconds = duration.toLong(),
            message_id = messageId,
            date = date
        )
        Unit
    }

    override suspend fun getLastSessionForMessage(messageId: Long): Session? =
        withContext(Dispatchers.Default) {
            corgeQueries.selectLastByMessage(messageId).executeAsOneOrNull()?.toModel()
        }

    override suspend fun resetProgress() = withContext(Dispatchers.Default) {
        corgeQueries.transaction {
            corgeQueries.deleteAllFavorite()
            corgeQueries.deleteAllSession()
        }
        Unit
    }
}