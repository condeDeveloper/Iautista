package com.count.iautista.data.audio

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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

    /** True durante síntese/download — exibe spinner no card. */
    private val _isSynthesizing = MutableStateFlow(false)
    val isSynthesizing: StateFlow<Boolean> = _isSynthesizing.asStateFlow()

    /** True durante reprodução do áudio — exibe ícone de som no card. */
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureLanguage()
                configureUtteranceListener()
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

    private fun configureUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSynthesizing.value = false
                _isPlaying.value = true
            }
            override fun onDone(utteranceId: String?) {
                _isPlaying.value = false
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSynthesizing.value = false
                _isPlaying.value = false
            }
        })
    }

    // ── API pública ──────────────────────────────────────────────────────────

    fun speak(text: String) {
        if (text.isBlank()) return
        _isSynthesizing.value = true
        _isPlaying.value = false
        if (azureTts.isConfigured) {
            scope.launch {
                try {
                    val file = azureTts.synthesize(text)
                    if (file != null) {
                        // _isSynthesizing permanece true; transição para _isPlaying em onPrepared
                        playAudioFile(file.absolutePath)
                    } else {
                        speakWithAndroid(text)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro Azure TTS: ${e.message}")
                    speakWithAndroid(text)
                }
            }
        } else {
            speakWithAndroid(text)
        }
    }

    fun speakOrPlayAudio(text: String, audioUri: String?) {
        if (!audioUri.isNullOrBlank()) {
            _isSynthesizing.value = true
            _isPlaying.value = false
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
        _isPlaying.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isAndroidTtsReady = false
        releasePlayer()
        _isSynthesizing.value = false
        _isPlaying.value = false
    }

    // ── Android TTS ──────────────────────────────────────────────────────────

    private fun speakWithAndroid(text: String) {
        if (!isAndroidTtsReady) {
            pendingQueue.add(text)
            return // mantém _isSynthesizing=true; transição ocorre quando TTS fica pronto
        }
        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        // _isSynthesizing permanece true; UtteranceProgressListener.onStart faz a transição
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, UUID.randomUUID().toString())
    }

    // ── Reprodução de arquivo de áudio ───────────────────────────────────────

    private fun playAudioFile(uri: String) {
        releasePlayer()
        try {
            currentPlayer = MediaPlayer().apply {
                setDataSource(uri)
                setOnPreparedListener { mp ->
                    _isSynthesizing.value = false
                    _isPlaying.value = true
                    mp.start()
                }
                setOnCompletionListener {
                    releasePlayer()
                    _isPlaying.value = false
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer erro what=$what extra=$extra")
                    releasePlayer()
                    _isSynthesizing.value = false
                    _isPlaying.value = false
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao reproduzir: $uri", e)
            currentPlayer = null
            _isSynthesizing.value = false
            _isPlaying.value = false
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
