package org.app.corge.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.app.corge.data.model.Category
import org.app.corge.data.model.Message
import org.app.corge.data.repository.CorgeRepository
import org.app.corge.data.repository.SettingsRepository
import org.app.corge.sound.SoundController
import org.app.corge.sound.TtsController
import kotlin.random.Random

class HomeViewModel(
    private val repo: CorgeRepository,
    private val sound: SoundController,
    private val tts: TtsController,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var todayKey: String = ""
    private var activeMessageId: Long? = null
    private var observeJob: Job? = null

    init {
        viewModelScope.launch {
            repo.prepopulateIfNeeded()

            launch {
                sound.isPlayingFlow.collect { playing ->
                    val current = _uiState.value
                    if (current is HomeUiState.Loaded) {
                        _uiState.value = current.copy(isSoundPlaying = playing)
                    }
                }
            }

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            todayKey = "${now.date}"

            val categories = repo.getAllCategories()

            val todaySessions = repo.getSessionByDate(todayKey)

            val existingToday = todaySessions.lastOrNull()
            activeMessageId = existingToday?.messageId

            delay(800)

            val isFirstHomeStart = settings.isFirstHomeStart()
            if (isFirstHomeStart) {
                _uiState.value = HomeUiState.Empty
                return@launch
            }

            if (existingToday == null) {
                createNewDailyMessage(categories, now)
            } else {
                startObserving(categories, now)
            }
        }
    }

    private suspend fun createNewDailyMessage(categories: List<Category>, now: LocalDateTime) {
        val all = repo.getAllMessages()
        val seed = now.date.dayOfYear
        val msg = all.shuffled(kotlin.random.Random(seed)).firstOrNull() ?: return

        repo.insertSession(
            messageId = msg.id,
            note = null,
            done = false,
            duration = 0
        )

        activeMessageId = msg.id
        startObserving(categories, now)
    }

    private fun startObserving(categories: List<Category>, now: LocalDateTime) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            val msgId = activeMessageId
            if (msgId == null) {
                _uiState.value = HomeUiState.Empty
                return@launch
            }

            repo.sessionFlow(msgId, todayKey).collect { session ->
                if (session == null) {
                    _uiState.value = HomeUiState.Empty
                } else {
                    val msg = repo.getMessageById(msgId)
                    if (msg == null) {
                        _uiState.value = HomeUiState.Empty
                    } else {
                        _uiState.value = HomeUiState.Loaded(
                            message = msg,
                            isDoneToday = session.done,
                            note = session.note,
                            categories = categories,
                            explore = exploreLinesFor(now.date.dayOfYear),
                            isSoundPlaying = sound.isPlaying
                        )
                    }
                }
            }
        }
    }

    private fun exploreLinesFor(seed: Int): List<String> =
        listOf(
            "Let the world flow through you without ever touching your peace.",
            "Everything you seek is already quietly resting inside you.",
            "Every moment that passes is already a precious gift of life.",
            "One single step forward is already the start of your journey.",
            "Let go of what holds you, and you’ll become lighter than the wind.",
            "Breathe deeply — the world around you is in no hurry to change."
        ).shuffled(kotlin.random.Random(seed)).take(6)

    fun startFirstSession() {
        viewModelScope.launch {
            settings.setFirstHomeStart(false)

            repo.prepopulateIfNeeded()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            todayKey = "${now.date}"

            val all = repo.getAllMessages()
            val seed = now.date.dayOfYear
            val msg = all.shuffled(kotlin.random.Random(seed)).firstOrNull() ?: return@launch

            repo.insertSession(
                messageId = msg.id,
                note = null,
                done = false,
                duration = 0
            )

            activeMessageId = msg.id
            val categories = repo.getAllCategories()
            startObserving(categories, now)
        }
    }

    fun toggleDone() {
        val loaded = uiState.value as? HomeUiState.Loaded ?: return
        val msgId = loaded.message.id
        viewModelScope.launch {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            repo.setSessionDoneByMessageAndDate(msgId, today, !loaded.isDoneToday)
        }
    }

    fun toggleSound() {
        if (sound.isPlaying) sound.stop() else sound.startLoop("calm_loop.mp3")

        val loaded = _uiState.value as? HomeUiState.Loaded ?: return
        _uiState.value = loaded.copy(isSoundPlaying = sound.isPlaying)
    }

    fun readAloud() {
        val text = (uiState.value as? HomeUiState.Loaded)?.message?.textEn ?: return
        tts.speak(text)
    }

    fun saveNote(note: String) {
        val msgId = (uiState.value as? HomeUiState.Loaded)?.message?.id ?: activeMessageId ?: return
        val newNote = note.take(280)

        val loaded = _uiState.value as? HomeUiState.Loaded
        if (loaded != null) _uiState.value = loaded.copy(note = newNote)

        viewModelScope.launch {
            repo.insertSession(
                messageId = msgId,
                note = newNote,
                done = (uiState.value as? HomeUiState.Loaded)?.isDoneToday == true,
                duration = 0
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val categories = repo.getAllCategories()
            startObserving(categories, now)
        }
    }

    override fun onCleared() {
        sound.stop()
        tts.stop()
        observeJob?.cancel()
        super.onCleared()
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data object Empty : HomeUiState()
    data class Loaded(
        val message: Message,
        val isDoneToday: Boolean,
        val note: String?,
        val categories: List<Category>,
        val explore: List<String>,
        val isSoundPlaying: Boolean = false
    ) : HomeUiState()
}
