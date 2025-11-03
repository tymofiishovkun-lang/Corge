package org.app.corge.screens.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.app.corge.createShareFile
import org.app.corge.data.model.Message
import org.app.corge.data.repository.CorgeRepository
import org.app.corge.sound.SoundController

class DetailsViewModel(
    private val repo: CorgeRepository,
    private val sound: SoundController,
) : ViewModel() {

    var ui by mutableStateOf<DetailsUiState>(DetailsUiState.Loading)
        private set

    private var messageId: Long = -1L
    private var selectedDate: String? = null

    private var favJob: Job? = null
    private var sessionJob: Job? = null
    private var soundJob: Job? = null

    fun load(id: Long, date: String? = null) {
        if (messageId == id && selectedDate == date && ui is DetailsUiState.Loaded) return

        messageId = id
        selectedDate = date

        favJob?.cancel()
        favJob = viewModelScope.launch {
            repo.favoriteIdsFlow().collect { ids ->
                val st = ui as? DetailsUiState.Loaded ?: return@collect
                val fav = ids.contains(messageId)
                if (st.isFavorite != fav)
                    ui = st.copy(isFavorite = fav)
            }
        }

        viewModelScope.launch {
            ui = DetailsUiState.Loading

            val msg = repo.getMessageById(id) ?: run {
                ui = DetailsUiState.Error
                return@launch
            }

            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date.toString()
            val dateKey = selectedDate ?: today

            val currentFavs = repo.getFavorites()
            val isFav = currentFavs.any { it.id == id }

            ui = DetailsUiState.Loaded(
                message = msg,
                isFavorite = isFav,
                isDoneToday = false,
                note = "",
                isSoundOn = sound.isPlaying,
                isBusy = false
            )

            sessionJob?.cancel()
            sessionJob = viewModelScope.launch {
                repo.sessionFlow(messageId, dateKey).collect { s ->
                    val cur = ui as? DetailsUiState.Loaded ?: return@collect
                    ui = cur.copy(
                        isDoneToday = (s?.done == true),
                        note = s?.note.orEmpty()
                    )
                }
            }

            soundJob?.cancel()
            soundJob = viewModelScope.launch {
                sound.isPlayingFlow.collect { playing ->
                    val cur = ui as? DetailsUiState.Loaded ?: return@collect
                    if (cur.isSoundOn != playing)
                        ui = cur.copy(isSoundOn = playing)
                }
            }
        }
    }

    fun toggleFavorite() = launchBusy {
        repo.toggleFavorite(messageId)
    }

    fun createShareFileForMessage(msg: String): String {
        return createShareFile(msg)
    }

    fun onNoteChange(newValue: String) {
        val st = ui as? DetailsUiState.Loaded ?: return
        ui = st.copy(note = newValue.take(280))
    }

    fun saveNote() = launchBusy {
        val st = ui as? DetailsUiState.Loaded ?: return@launchBusy
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date.toString()
        val dateKey = selectedDate ?: today

        repo.insertSession(
            messageId = messageId,
            note = st.note.take(280),
            done = st.isDoneToday,
            duration = 0,
            date = dateKey
        )
    }

    fun toggleSound() {
        if (sound.isPlaying) sound.stop()
        else sound.startLoop("calm_loop.mp3")
    }

    override fun onCleared() {
        sound.stop()
        favJob?.cancel()
        sessionJob?.cancel()
        soundJob?.cancel()
        super.onCleared()
    }

    private fun launchBusy(block: suspend () -> Unit) {
        val st = ui as? DetailsUiState.Loaded ?: return
        if (st.isBusy) return
        viewModelScope.launch {
            ui = st.copy(isBusy = true)
            try {
                block()
            } finally {
                (ui as? DetailsUiState.Loaded)?.let {
                    ui = it.copy(isBusy = false)
                }
            }
        }
    }
}

sealed class DetailsUiState {
    data object Loading : DetailsUiState()
    data object Error   : DetailsUiState()
    data class Loaded(
        val message: Message,
        val isFavorite: Boolean,
        val isDoneToday: Boolean,
        val note: String,
        val isSoundOn: Boolean,
        val isBusy: Boolean
    ) : DetailsUiState()
}