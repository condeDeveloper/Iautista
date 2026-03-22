package com.count.iautista.data.audio

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private val pendingQueue = mutableListOf<String>()
    private var currentPlayer: MediaPlayer? = null

    init {
        tts = TextToSpeech(context) { status ->
            Log.d(TAG, "TTS onInit status=$status")
            if (status == TextToSpeech.SUCCESS) {
                configureLanguage()
                isReady = true
                // Fala tudo que ficou enfileirado durante a inicialização
                val pending = pendingQueue.toList()
                pendingQueue.clear()
                pending.forEach { doSpeak(it) }
            } else {
                Log.e(TAG, "TTS init falhou com status=$status")
            }
        }
    }

    private fun configureLanguage() {
        val locales = listOf(Locale("pt", "BR"), Locale("pt"), Locale.getDefault())
        for (locale in locales) {
            val result = tts?.setLanguage(locale)
            Log.d(TAG, "TTS setLanguage($locale) = $result")
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d(TAG, "TTS usando idioma: $locale")
                return
            }
        }
        Log.w(TAG, "TTS: nenhum idioma preferido disponível, usando padrão do engine")
    }

    // ── API pública ──────────────────────────────────────────────────────────

    fun speak(text: String) {
        if (text.isBlank()) return
        Log.d(TAG, "speak() isReady=$isReady text='$text'")
        if (!isReady) {
            pendingQueue.add(text)
            return
        }
        doSpeak(text)
    }

    private fun doSpeak(text: String) {
        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, UUID.randomUUID().toString())
        Log.d(TAG, "tts.speak() result=$result")
    }

    fun speakOrPlayAudio(text: String, audioUri: String?) {
        if (!audioUri.isNullOrBlank()) {
            playAudioFile(audioUri)
        } else {
            speak(text)
        }
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.1f, 2.0f))
    }

    fun stop() {
        tts?.stop()
        releasePlayer()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
        releasePlayer()
    }

    // ── Áudio customizado ────────────────────────────────────────────────────

    private fun playAudioFile(uri: String) {
        releasePlayer()
        try {
            currentPlayer = MediaPlayer().apply {
                setDataSource(uri)
                setOnCompletionListener { releasePlayer() }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer erro what=$what extra=$extra")
                    releasePlayer()
                    false
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao reproduzir áudio: $uri", e)
            currentPlayer = null
        }
    }

    private fun releasePlayer() {
        currentPlayer?.runCatching {
            if (isPlaying) stop()
            release()
        }
        currentPlayer = null
    }

    companion object {
        private const val TAG = "TtsManager"
    }
}
