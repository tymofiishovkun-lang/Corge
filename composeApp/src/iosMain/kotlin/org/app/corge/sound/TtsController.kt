package org.app.corge.sound

import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.AVSpeechUtteranceDefaultSpeechRate

actual class TtsController {

    private val synthesizer: AVSpeechSynthesizer = AVSpeechSynthesizer()

    actual fun speak(text: String) {
        if (text.isBlank()) return

        if (synthesizer.speaking) {
            synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        }

        val utterance: AVSpeechUtterance = AVSpeechUtterance(string = text)

        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage("en-US")

        utterance.rate = 0.5f

        utterance.preUtteranceDelay = 0.0
        utterance.postUtteranceDelay = 0.0

        synthesizer.speakUtterance(utterance)
    }

    actual fun stop() {
        if (synthesizer.speaking) {
            synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        }
    }
}