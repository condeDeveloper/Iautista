package com.count.iautista.ui.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.math.PI
import kotlin.math.sin

val LocalSoundManager = staticCompositionLocalOf<SoundManager> {
    error("LocalSoundManager não foi fornecido na árvore de composição")
}

/**
 * Gera e reproduz sons leves diretamente por PCM — sem arquivos de áudio.
 * Dois sons:
 *   [playTap]      — toque leve (880Hz / 70ms): feedback em botões e cards
 *   [playComplete] — acorde ascendente (C5 → G5): confirmação de conclusão
 */
class SoundManager {

    private val sampleRate = 44100

    // Buffers pré-calculados: zero overhead em tempo de tap
    private val tapBuffer      = tone(880.0,   70, 0.18f)
    private val completeBuffer = chime(523.25, 783.99)

    fun playTap()      = playBuffer(tapBuffer)
    fun playComplete() = playBuffer(completeBuffer)

    // ── Geração de áudio ─────────────────────────────────────────────────────

    private fun tone(freqHz: Double, durationMs: Int, volume: Float): ShortArray {
        val n = sampleRate * durationMs / 1000
        return ShortArray(n) { i ->
            val t = i.toDouble() / sampleRate
            (sin(2 * PI * freqHz * t) * envelope(i, n) * Short.MAX_VALUE * volume)
                .toInt().toShort()
        }
    }

    /**
     * Dois tons separados por uma pausa curta — soa como um "ding-dong" suave.
     * Nota 1: C5 (523Hz) · 160ms
     * Nota 2: G5 (784Hz) · 200ms
     * Pausa entre elas: 80ms de silêncio
     */
    private fun chime(freq1: Double, freq2: Double): ShortArray {
        val n1    = sampleRate * 160 / 1000
        val pause = sampleRate * 80  / 1000
        val n2    = sampleRate * 200 / 1000
        val total = n1 + pause + n2
        val buf   = ShortArray(total)

        for (i in 0 until n1) {
            val t = i.toDouble() / sampleRate
            buf[i] = (sin(2 * PI * freq1 * t) * envelope(i, n1) * Short.MAX_VALUE * 0.26f)
                .toInt().toShort()
        }
        val off = n1 + pause
        for (i in 0 until n2) {
            val t = i.toDouble() / sampleRate
            buf[i + off] = (sin(2 * PI * freq2 * t) * envelope(i, n2) * Short.MAX_VALUE * 0.26f)
                .toInt().toShort()
        }
        return buf
    }

    /** Fade-in rápido + fade-out suave — elimina cliques digitais nas bordas. */
    private fun envelope(i: Int, total: Int): Double {
        val fadeIn  = (total * 0.06).toInt().coerceAtLeast(1)
        val fadeOut = (total * 0.45).toInt().coerceAtLeast(1)
        return when {
            i < fadeIn           -> i.toDouble() / fadeIn
            i > total - fadeOut  -> (total - i).toDouble() / fadeOut
            else                 -> 1.0
        }
    }

    // ── Reprodução em thread separada ────────────────────────────────────────

    private fun playBuffer(buffer: ShortArray) {
        Thread {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(buffer, 0, buffer.size)
                track.play()
                Thread.sleep(buffer.size.toLong() * 1000 / sampleRate + 60)
                track.stop()
                track.release()
            } catch (_: Exception) { /* ignora erros de áudio */ }
        }.also { it.isDaemon = true }.start()
    }
}
