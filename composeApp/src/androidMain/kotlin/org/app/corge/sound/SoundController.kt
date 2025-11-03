package org.app.corge.sound

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.app.corge.AppContextProvider

actual class SoundController {
    private var player: MediaPlayer? = null
    private val context get() = AppContextProvider.get()

    private val _isPlaying = MutableStateFlow(false)
    actual val isPlayingFlow: StateFlow<Boolean> get() = _isPlaying

    actual val isPlaying: Boolean
        get() = _isPlaying.value

    actual fun startLoop(assetName: String) {
        stop()

        val (resName, _) = splitNameExt(assetName)
        val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
        try {
            val mp = if (resId != 0) {
                MediaPlayer.create(context, resId)
            } else {
                val afd = context.assets.openFd(assetName)
                MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    prepare()
                }
            }
            mp.isLooping = true
            mp.start()
            player = mp
            _isPlaying.value = true
        } catch (_: Throwable) {
            stop()
        }
    }

    actual fun stop() {
        try {
            player?.stop()
        } catch (_: Throwable) {}
        player?.release()
        player = null
        _isPlaying.value = false
    }

    private fun splitNameExt(name: String): Pair<String, String?> {
        val dot = name.lastIndexOf('.')
        return if (dot in 1 until name.length - 1) {
            name.substring(0, dot) to name.substring(dot + 1)
        } else name to null
    }
}