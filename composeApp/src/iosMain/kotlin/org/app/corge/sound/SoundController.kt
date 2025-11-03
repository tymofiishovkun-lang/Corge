package org.app.corge.sound

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSError

@OptIn(ExperimentalForeignApi::class)
actual class SoundController {

    private var player: AVAudioPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    actual val isPlayingFlow: StateFlow<Boolean> get() = _isPlaying

    actual val isPlaying: Boolean
        get() = _isPlaying.value

    actual fun startLoop(assetName: String) {
        stop()

        val (name, ext) = splitNameExt(assetName)
        val url = NSBundle.mainBundle.URLForResource(name, ext) ?: return

        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)

        val p = AVAudioPlayer(contentsOfURL = url, error = null)
        p.numberOfLoops = -1
        p.prepareToPlay()
        p.play()

        player = p
        _isPlaying.value = true
    }

    actual fun stop() {
        player?.stop()
        player = null
        AVAudioSession.sharedInstance().setActive(false, error = null)
        _isPlaying.value = false
    }

    private fun splitNameExt(assetName: String): Pair<String, String> {
        val dot = assetName.lastIndexOf('.')
        return if (dot >= 0) {
            assetName.substring(0, dot) to assetName.substring(dot + 1)
        } else {
            assetName to "mp3"
        }
    }
}