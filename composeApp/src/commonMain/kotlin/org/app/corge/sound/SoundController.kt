package org.app.corge.sound

import kotlinx.coroutines.flow.StateFlow

expect class SoundController() {
    fun startLoop(assetName: String)
    fun stop()
    val isPlaying: Boolean
    val isPlayingFlow: StateFlow<Boolean>
}