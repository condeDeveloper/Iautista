package com.count.iautista.data.audio

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val azureTts: AzureTtsService,
) {
    private var tts: TextToSpeech? = null
    private var isAndroidTtsReady = false
    private val pendingQueue = mutableListOf<String>()
    private var currentPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isSynthesizing = MutableStateFlow(false)
    /**
     * True desde o momento do clique até o áudio terminar de tocar.
     * Cobre síntese (rede) + reprodução — para a UI manter o loading durante todo o processo.
     */
    val isSynthesizing: StateFlow<Boolean> = _isSynthesizing.asStateFlow()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureLanguage()
                isAndroidTtsReady = true
                val pending = pendingQueue.toList()
                pendingQueue.clear()
                pending.forEach { speakWithAndroid(it) }
            } else {
                Log.e(TAG, "Android TTS init falhou: $status")
            }
        }
    }

    private fun configureLanguage() {
        val locales = listOf(Locale("pt", "BR"), Locale("pt"), Locale.getDefault())
        for (locale in locales) {
            val result = tts?.setLanguage(locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d(TAG, "Android TTS usando: $locale")
                return
            }
        }
    }

    // ── API pública ──────────────────────────────────────────────────────────

    fun speak(text: String) {
        if (text.isBlank()) return
        _isSynthesizing.value = true
        if (azureTts.isConfigured) {
            scope.launch {
                try {
                    val file = azureTts.synthesize(text)
                    if (file != null) {
                        // _isSynthesizing fica true até playAudioFile notificar conclusão
                        playAudioFile(file.absolutePath)
                    } else {
                        speakWithAndroid(text)
                        _isSynthesizing.value = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro Azure TTS: ${e.message}")
                    speakWithAndroid(text)
                    _isSynthesizing.value = false
                }
            }
        } else {
            speakWithAndroid(text)
            _isSynthesizing.value = false
        }
    }

    fun speakOrPlayAudio(text: String, audioUri: String?) {
        if (!audioUri.isNullOrBlank()) {
            _isSynthesizing.value = true
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
        _isSynthesizing.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isAndroidTtsReady = false
        releasePlayer()
        _isSynthesizing.value = false
    }

    // ── Android TTS ──────────────────────────────────────────────────────────

    private fun speakWithAndroid(text: String) {
        if (!isAndroidTtsReady) {
            pendingQueue.add(text)
            return
        }
        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, UUID.randomUUID().toString())
    }

    // ── Reprodução de arquivo de áudio ───────────────────────────────────────

    private fun playAudioFile(uri: String) {
        releasePlayer()
        try {
            currentPlayer = MediaPlayer().apply {
                setDataSource(uri)
                setOnCompletionListener {
                    releasePlayer()
                    _isSynthesizing.value = false
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer erro what=$what extra=$extra")
                    releasePlayer()
                    _isSynthesizing.value = false
                    false
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao reproduzir: $uri", e)
            currentPlayer = null
            _isSynthesizing.value = false
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
