package org.app.corge.sound

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import org.app.corge.AppContextProvider
import java.util.Locale

actual class TtsController {
    private var tts: TextToSpeech? = null
    private val ctx get() = AppContextProvider.get()

    actual fun speak(text: String) {
        if (text.isBlank()) return

        val speakBlock: (TextToSpeech) -> Unit = { inst ->
            val bestVoice = inst.voices
                ?.filter { it.locale?.language == "en" }
                ?.sortedWith(
                    compareByDescending<Voice> { it.quality }
                        .thenBy { if (it.isNetworkConnectionRequired) 0 else 1 }
                )
                ?.firstOrNull()

            if (bestVoice != null) {
                inst.voice = bestVoice
            } else {
                inst.language = Locale.US
            }

            inst.setSpeechRate(0.95f)
            inst.setPitch(0.98f)

            inst.speak(text, TextToSpeech.QUEUE_FLUSH, null, "home-tts")
        }

        val existing = tts
        if (existing == null) {
            val listener = TextToSpeech.OnInitListener { status ->
                if (status != TextToSpeech.SUCCESS) return@OnInitListener
                val inst = tts ?: return@OnInitListener
                speakBlock(inst)
            }
            tts = TextToSpeech(ctx, listener)
        } else {
            existing.stop()
            speakBlock(existing)
        }
    }

    actual fun stop() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}