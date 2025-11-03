package org.app.corge.screens.search

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.app.corge.data.content.BundledContent
import org.app.corge.data.model.Category
import org.app.corge.data.model.Message
import org.app.corge.data.model.MessageType
import org.app.corge.data.repository.CorgeRepository
import org.app.corge.data.repository.SettingsRepository

class SearchViewModel(
    private val repo: CorgeRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    var state by mutableStateOf(SearchUiState())
        private set

    private val todayKey: String = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

    init {
        viewModelScope.launch {
            repo.doneIdsFlow().collect { ids ->
                state = state.copy(doneIds = ids)
            }
        }
        viewModelScope.launch {
            settings.recentSearchesFlow().collect { recents ->
                state = state.copy(recentQueries = recents)
            }
        }
    }

    fun load() {
        if (!state.loadedOnce) {
            viewModelScope.launch {
                val cats = withContext(Dispatchers.Default) { repo.getAllCategories() }
                state = state.copy(
                    categories = cats,
                    loadedOnce = true
                )
            }
        }
    }

    fun toFilters() {
        state = state.copy(mode = SearchMode.Filters)
    }

    fun selectCategoryAndSearch(categoryId: String) {
        state = state.copy(
            selectedCategoryIds = setOf(categoryId),
            query = "",
            selectedType = null,
            onlyUnread = false,
            onlyFavorites = false,
            mode = SearchMode.Results
        )
        applyFilters()
    }

    fun onQueryChange(q: String) { state = state.copy(query = q.take(200)) }

    fun toggleType(t: TypeFilter) {
        state = state.copy(selectedType =
            if (state.selectedType == t) null else t
        )
    }

    fun toggleCategory(id: String) {
        val next = state.selectedCategoryIds.toMutableSet()
        if (!next.add(id)) next.remove(id)
        state = state.copy(selectedCategoryIds = next)
    }

    fun toggleUnread()   { state = state.copy(onlyUnread = !state.onlyUnread) }
    fun toggleFavs()     { state = state.copy(onlyFavorites = !state.onlyFavorites) }

    fun clearAll() {
        state = state.copy(
            query = "",
            selectedType = null,
            selectedCategoryIds = emptySet(),
            onlyUnread = false,
            onlyFavorites = false,
            mode = SearchMode.Filters
        )
    }

    fun resetFiltersFromEmpty() = clearAll()

    fun applyFilters() {
        viewModelScope.launch {
            val st = state

            val rq = st.query.trim().takeIf { it.isNotEmpty() }
            val recent = if (rq != null) {
                (listOf(rq) + st.recentQueries.filter { it != rq }).take(5)
            } else st.recentQueries
            settings.setRecentSearches(recent)
            if (rq != null) settings.addRecentSearch(rq)

            val all    = repo.getAllMessages()
            val favId  = repo.getFavorites().map { it.id }.toSet()
            val doneId = repo.getDoneMessageIds().toSet()

            val filtered = all.asSequence()
                .filter { m -> st.selectedType?.matches(m.type) ?: true }
                .filter { m ->
                    if (st.selectedCategoryIds.isEmpty()) true
                    else st.selectedCategoryIds.any { id ->
                        id.equals(m.category, ignoreCase = true) ||
                                BundledContent.categories
                                    .firstOrNull { it.id.equals(id, ignoreCase = true) }
                                    ?.title.equals(m.category, ignoreCase = true)
                    }
                }
                .filter { m -> if (st.onlyFavorites) favId.contains(m.id) else true }
                .filter { m -> if (st.onlyUnread) !doneId.contains(m.id) else true }
                .filter { m ->
                    val q = st.query.trim()
                    if (q.isEmpty()) true else m.textEn.contains(q, ignoreCase = true)
                }
                .toList()

            val dates: Map<Long, String> = withContext(Dispatchers.Default) {
                filtered.associate { m ->
                    val last = repo.getLastSessionForMessage(m.id)
                    m.id to (last?.date ?: "")
                }
            }

            state = st.copy(
                recentQueries = recent,
                results = filtered,
                doneIds = doneId,
                lastDateByMessageId = dates,
                mode = if (filtered.isEmpty()) SearchMode.Empty else SearchMode.Results
            )
        }
    }

    fun refreshDoneIds() {
        viewModelScope.launch {
            val ids = repo.getDoneMessageIds()
            state = state.copy(doneIds = ids)
        }
    }

    fun clearRecents() {
        viewModelScope.launch {
            settings.clearRecentSearches()
            state = state.copy(recentQueries = emptyList())
        }
    }

    fun useRecentQuery(q: String) {
        state = state.copy(query = q)
    }

    fun setSort(mode: SortMode) {
        state = state.copy(sort = mode)
    }
}

enum class SearchMode { Filters, Results, Empty }

enum class SortMode { Recommended, AZ }

enum class TypeFilter { Phrase, Breathing, Task;
    fun label(): String = when (this) {
        Phrase -> "Phrase"
        Breathing -> "Breathing practice"
        Task -> "Task"
    }
    fun matches(type: String): Boolean {
        val t = type.lowercase()
        return when (this) {
            Phrase -> t == "phrase"
            Breathing -> t == "breathing"
            Task -> t == "task"
        }
    }

    fun matches(type: MessageType): Boolean = when (this) {
        Phrase -> type == MessageType.PHRASE
        Breathing -> type == MessageType.BREATHING
        Task -> type == MessageType.TASK
    }

}

@Immutable
data class SearchUiState(
    val loadedOnce: Boolean = false,
    val query: String = "",
    val selectedType: TypeFilter? = null,
    val categories: List<Category> = emptyList(),
    val selectedCategoryIds: Set<String> = emptySet(),
    val onlyUnread: Boolean = false,
    val onlyFavorites: Boolean = false,
    val recentQueries: List<String> = emptyList(),
    val results: List<Message> = emptyList(),
    val sort: SortMode = SortMode.Recommended,
    val mode: SearchMode = SearchMode.Filters,
    val doneIds: Set<Long> = emptySet(),
    val selectedDate: String? = null,
    val lastDateByMessageId: Map<Long, String> = emptyMap()
)