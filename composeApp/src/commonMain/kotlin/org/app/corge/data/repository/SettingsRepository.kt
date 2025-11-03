package org.app.corge.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface SettingsRepository {
    suspend fun isFirstLaunch(): Boolean
    suspend fun setFirstLaunch(value: Boolean)
    suspend fun getRecentSearches(): List<String>
    suspend fun setRecentSearches(items: List<String>)
    suspend fun addRecentSearch(query: String)
    suspend fun clearRecentSearches()
    fun recentSearchesFlow(): Flow<List<String>>
}

private const val KEY_FIRST_LAUNCH = "first_launch"
private const val KEY_RECENTS = "recent_searches_v1"
private const val SEP = "\u001F"
private const val ESC = "\\"

private fun encode(items: List<String>): String =
    items.joinToString(SEP) { it.replace(ESC, ESC + ESC).replace(SEP, ESC + SEP) }

private fun decode(raw: String): List<String> {
    val out = mutableListOf<String>()
    val sb  = StringBuilder()
    var i = 0
    while (i < raw.length) {
        val ch = raw[i].toString()
        when {
            ch == ESC && i + 1 < raw.length -> {
                sb.append(raw[i + 1])
                i += 2
            }
            ch == SEP -> { out += sb.toString(); sb.clear(); i++ }
            else -> { sb.append(ch); i++ }
        }
    }
    out += sb.toString()
    return out
}

class SettingsRepositoryImpl(
    private val settings: Settings
) : SettingsRepository {

    private val _recents = MutableStateFlow(
        runCatching { settings.getStringOrNull(KEY_RECENTS)?.let(::decode) ?: emptyList() }
            .getOrElse { emptyList() }
    )

    override suspend fun isFirstLaunch(): Boolean =
        settings.getBoolean(KEY_FIRST_LAUNCH, true)

    override suspend fun setFirstLaunch(value: Boolean) {
        settings.putBoolean(KEY_FIRST_LAUNCH, value)
    }

    override suspend fun getRecentSearches(): List<String> {
        val raw = settings.getStringOrNull(KEY_RECENTS) ?: return emptyList()
        return runCatching { decode(raw) }.getOrElse { emptyList() }
    }

    override suspend fun setRecentSearches(items: List<String>) {
        val top5 = items.take(5)
        settings.putString(KEY_RECENTS, encode(top5))
        _recents.value = top5
    }

    override suspend fun addRecentSearch(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        val next = (listOf(q) + _recents.value.filter { it != q }).take(5)
        settings.putString(KEY_RECENTS, encode(next))
        _recents.value = next
    }

    override suspend fun clearRecentSearches() {
        settings.remove(KEY_RECENTS)
        _recents.value = emptyList()
    }

    override fun recentSearchesFlow(): Flow<List<String>> = _recents
}