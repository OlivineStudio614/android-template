package com.olivinestudio614.hartmannav.hartman

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class HartmanTTS(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false
    private val queue = mutableListOf<String>()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(1.15f)
                ready = true
                queue.forEach { speak(it) }
                queue.clear()
            }
        }
    }

    fun speak(text: String) {
        if (!ready) {
            queue += text
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, text.hashCode().toString())
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
