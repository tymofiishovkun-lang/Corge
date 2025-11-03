package org.app.corge.sound

expect class TtsController() {
    fun speak(text: String)
    fun stop()
}