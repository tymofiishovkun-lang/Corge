package org.app.corge.screens.journal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.app.corge.data.model.Category
import org.app.corge.data.model.Message
import org.app.corge.data.model.Session
import org.app.corge.data.repository.CorgeRepository
import org.app.corge.screens.search.TypeFilter

class JournalViewModel(
    private val repo: CorgeRepository
) : ViewModel() {

    data class Item(
        val session: Session,
        val message: Message,
        val isEditing: Boolean = false,
        val draft: String = session.note.orEmpty()
    )

    data class Ui(
        val loading: Boolean = true,
        val type: TypeFilter? = null,
        val categories: List<Category> = emptyList(),
        val selectedCats: Set<String> = emptySet(),
        val groups: Map<String, List<Item>> = emptyMap(),
        val showConfirmUnsetDone: Item? = null,
        val showDeleteNoteFor: Item? = null
    )

    var ui by mutableStateOf(Ui())
        private set

    private var allItems: List<Item> = emptyList()

    private var idByTitle: Map<String, String> = emptyMap()
    private var titleById: Map<String, String> = emptyMap()

    fun load(limit: Int = 50) {
        viewModelScope.launch {
            ui = ui.copy(loading = true)

            val cats = repo.getAllCategories()

            idByTitle = cats.associate { it.title.lowercase() to it.id }
            titleById = cats.associate { it.id.lowercase() to it.title }

            val sessions = repo.getRecentSessions(limit)
            val items = sessions.mapNotNull { s ->
                val m = repo.getMessageById(s.messageId) ?: return@mapNotNull null
                Item(session = s, message = m)
            }

            allItems = items
            ui = ui.copy(
                loading = false,
                categories = cats
            )
            applyFilters()
        }
    }

    fun toggleType(f: TypeFilter) {
        ui = ui.copy(type = if (ui.type == f) null else f)
        applyFilters()
    }

    fun toggleCategory(id: String) {
        val next = ui.selectedCats.toMutableSet()
        if (!next.add(id)) next.remove(id)
        ui = ui.copy(selectedCats = next)
        applyFilters()
    }

    fun clearFilters() {
        ui = ui.copy(type = null, selectedCats = emptySet())
        applyFilters()
    }

    fun startEdit(date: String, msgId: Long) {
        mutateItem(msgId) { it.copy(isEditing = true, draft = it.session.note.orEmpty()) }
    }

    fun editDraft(date: String, msgId: Long, text: String) {
        mutateItem(msgId) { it.copy(draft = text.take(280)) }
    }

    fun cancelEdit(date: String, msgId: Long) {
        mutateItem(msgId) { it.copy(isEditing = false) }
    }

    fun saveNote(date: String, msgId: Long) {
        val item = allItems.firstOrNull { it.message.id == msgId } ?: return
        viewModelScope.launch {
            repo.updateSessionByMessageAndDate(
                messageId = item.message.id,
                date = date,
                done = item.session.done,
                duration = item.session.durationSeconds,
                note = item.draft.ifBlank { null }
            )
            mutateItem(msgId) {
                it.copy(
                    isEditing = false,
                    session = it.session.copy(note = it.draft.ifBlank { null })
                )
            }
        }
    }

    fun askDeleteNote(date: String, msgId: Long) {
        ui = ui.copy(showDeleteNoteFor = allItems.firstOrNull { it.message.id == msgId })
    }
    fun dismissDeleteNote() { ui = ui.copy(showDeleteNoteFor = null) }

    fun deleteNoteConfirmed() {
        val item = ui.showDeleteNoteFor ?: return
        viewModelScope.launch {
            repo.updateSessionByMessageAndDate(
                messageId = item.message.id,
                date = item.session.date,
                done = item.session.done,
                duration = item.session.durationSeconds,
                note = null
            )
            mutateItem(item.message.id) {
                it.copy(isEditing = false, draft = "", session = it.session.copy(note = null))
            }
            ui = ui.copy(showDeleteNoteFor = null)
        }
    }

    fun toggleDone(date: String, msgId: Long) {
        val item = allItems.firstOrNull { it.message.id == msgId } ?: return
        if (item.session.done) {
            ui = ui.copy(showConfirmUnsetDone = item)
        } else {
            setDone(date, msgId, true)
        }
    }
    fun dismissUnsetDone() { ui = ui.copy(showConfirmUnsetDone = null) }
    fun confirmUnsetDone() {
        val item = ui.showConfirmUnsetDone ?: return
        setDone(item.session.date, item.message.id, false)
        ui = ui.copy(showConfirmUnsetDone = null)
    }

    private fun setDone(date: String, msgId: Long, newValue: Boolean) {
        viewModelScope.launch {
            if (newValue) {
                repo.setSessionDoneByMessageAndDate(msgId, date, true)
            } else {
                repo.updateSessionByMessageAndDate(
                    messageId = msgId,
                    date = date,
                    done = false,
                    duration = 0,
                    note = null
                )
            }

            mutateItem(msgId) {
                it.copy(
                    session = it.session.copy(
                        done = newValue,
                        durationSeconds = if (newValue) it.session.durationSeconds else 0,
                        note = if (newValue) it.session.note else null
                    )
                )
            }
            ui = ui.copy(groups = ui.groups.toMap())
        }
    }
    private fun applyFilters() {
        val selected = ui.selectedCats.map { it.lowercase() }.toSet()

        fun messageCategoryMatches(messageCategory: String): Boolean {
            if (selected.isEmpty()) return true

            val raw = messageCategory.lowercase()
            if (raw in selected) return true
            val asId = idByTitle[raw]
            if (asId != null && asId.lowercase() in selected) return true
            val asTitle = titleById[raw]
            if (asTitle != null && asTitle.lowercase() in selected) return true

            return false
        }

        val filtered = allItems.asSequence()
            .filter { ui.type?.matches(it.message.type) ?: true }
            .filter { messageCategoryMatches(it.message.category) }
            .toList()

        val grouped: Map<String, List<Item>> = filtered.groupBy { it.session.date }

        val ordered: LinkedHashMap<String, List<Item>> = linkedMapOf()
        grouped.keys.sortedDescending().forEach { key ->
            grouped[key]?.let { ordered[key] = it }
        }

        ui = ui.copy(groups = ordered)
    }

    private fun mutateItem(msgId: Long, transform: (Item) -> Item) {
        allItems = allItems.map { if (it.message.id == msgId) transform(it) else it }
        applyFilters()
    }
}
