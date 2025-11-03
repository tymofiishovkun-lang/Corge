package org.app.corge.screens.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.app.corge.data.model.Message
import org.app.corge.data.model.MessageType
import org.app.corge.data.repository.CorgeRepository
import org.app.corge.sound.SoundController

class SessionViewModel(
    private val repo: CorgeRepository,
    private val sound: SoundController
) : ViewModel() {

    sealed class UiState {
        data object Loading : UiState()
        data object Error   : UiState()
        data class Loaded(
            val message: Message,
            val steps: List<String>,
            val stepIndex: Int = 0,
            val isStepsExpanded: Boolean = true,
            val isBreathing: Boolean = false,
            val isPlayingSound: Boolean = false,
            val elapsedSec: Int = 0,
            val targetSec: Int? = null,
            val showDoneDialog: Boolean = false,
            val noteDraft: String = "",
            val showExitConfirm: Boolean = false
        ) : UiState()
    }

    var ui by mutableStateOf<UiState>(UiState.Loading)
        private set

    private var tickJob: Job? = null
    private var soundJob: Job? = null
    private var currentMsgId: Long? = null

    fun load(messageId: Long) {
        if (currentMsgId == messageId && ui !is UiState.Loading) return
        currentMsgId = messageId

        viewModelScope.launch {
            val msg = repo.getMessageById(messageId) ?: run {
                ui = UiState.Error; return@launch
            }

            val steps = buildSteps(msg)

            ui = UiState.Loaded(
                message = msg,
                steps = steps,
                isBreathing = (msg.type == MessageType.BREATHING),
                targetSec = msg.durationSeconds?.takeIf { it > 0 },
                isPlayingSound = sound.isPlaying
            )

            soundJob?.cancel()
            soundJob = viewModelScope.launch {
                sound.isPlayingFlow.collect { playing ->
                    val cur = ui as? UiState.Loaded ?: return@collect
                    if (cur.isPlayingSound != playing)
                        ui = cur.copy(isPlayingSound = playing)
                }
            }
        }
    }

    fun askExitConfirm() {
        (ui as? UiState.Loaded)?.let { ui = it.copy(showExitConfirm = true) }
    }
    fun cancelExitConfirm() {
        (ui as? UiState.Loaded)?.let { ui = it.copy(showExitConfirm = false) }
    }

    fun resetSessionState() {
        pauseTimer()
        sound.stop()
        (ui as? UiState.Loaded)?.let { st ->
            ui = st.copy(
                stepIndex = 0,
                isStepsExpanded = true,
                elapsedSec = 0,
                isPlayingSound = false,
                showExitConfirm = false,
                noteDraft = ""
            )
        }
    }

    private fun buildSteps(m: Message): List<String> {
        val common = listOf(
            "Find a quiet place. It can be a room, a corner on the street, an armchair, or just a moment at the window. The main thing is that you feel calm.",
            "Sit or lie comfortably. Let the body relax. No poses or rules — just comfort.",
            "Read the task or phrase. Out loud or about yourself. Feel how it sounds inside you.",
            "Do a breathing practice. One or two cycles is enough. The main thing is to pay attention to breathing, not to technique.",
            "Be in silence. 1–2 minutes. Watch what's going on inside. Don't judge, just be.",
            "End with gratitude. Thank yourself for this moment. Even if it was short, it was yours."
        )

        return when {
            m.type == MessageType.BREATHING && !m.ritual.isNullOrBlank() ->
                listOf("Breathe in — soften the belly. Hold. Breathe out — soften the shoulders. Repeat slowly.") +
                        m.ritual!!.trim().split('\n').filter { it.isNotBlank() }

            m.type == MessageType.TASK && !m.ritual.isNullOrBlank() ->
                listOf(m.ritual!!.trim()) + common.takeLast(3)

            else -> common
        }
    }

    fun toggleStepsExpand() {
        val st = ui as? UiState.Loaded ?: return
        ui = st.copy(isStepsExpanded = !st.isStepsExpanded)
    }

    fun nextStep() {
        val st = ui as? UiState.Loaded ?: return
        val next = (st.stepIndex + 1).coerceAtMost(st.steps.lastIndex)
        ui = st.copy(stepIndex = next)
    }

    fun toggleSound() {
        if (sound.isPlaying) sound.stop()
        else sound.startLoop("calm_loop.mp3")
    }

    fun startTimer() {
        val st = ui as? UiState.Loaded ?: return
        if (tickJob != null) return
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val s0 = ui as? UiState.Loaded ?: continue
                val s1 = s0.copy(elapsedSec = s0.elapsedSec + 1)
                val reached = s1.targetSec?.let { s1.elapsedSec >= it } ?: false
                ui = s1
                if (reached) showDoneDialog()
            }
        }
    }

    fun pauseTimer() { tickJob?.cancel(); tickJob = null }

    fun showDoneDialog() {
        val st = ui as? UiState.Loaded ?: return
        pauseTimer()
        ui = st.copy(showDoneDialog = true)
    }

    fun hideDoneDialog() {
        val st = ui as? UiState.Loaded ?: return
        ui = st.copy(showDoneDialog = false)
    }

    fun editNote(text: String) {
        val st = ui as? UiState.Loaded ?: return
        ui = st.copy(noteDraft = text.take(280))
    }

    fun saveDoneAndNote(onFinished: () -> Unit) {
        val st = ui as? UiState.Loaded ?: return
        viewModelScope.launch {
            repo.insertSession(
                messageId = st.message.id,
                note = st.noteDraft.ifBlank { null },
                done = true,
                duration = st.elapsedSec
            )
            onFinished()
        }
    }

    override fun onCleared() {
        pauseTimer()
        sound.stop()
        soundJob?.cancel()
        super.onCleared()
    }
}
