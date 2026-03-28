package com.count.iautista.data.audio

import android.content.Context
import android.util.Log
import com.count.iautista.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sintetiza fala via Azure Neural TTS.
 * Voz: pt-BR-FranciscaNeural — feminina, natural, específica para português brasileiro.
 * Cache local: cada frase é baixada uma única vez e salva em disco.
 */
@Singleton
class AzureTtsService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val cacheDir = File(context.filesDir, "tts_cache").also { it.mkdirs() }

    val isConfigured: Boolean
        get() = BuildConfig.AZURE_TTS_KEY.isNotBlank() &&
                BuildConfig.AZURE_TTS_KEY != "SUA_CHAVE_AQUI"

    suspend fun synthesize(text: String): File? = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext null

        val cacheFile = cacheFileFor(text)
        if (cacheFile.exists() && cacheFile.length() > 0) {
            Log.d(TAG, "Cache hit: ${cacheFile.name}")
            return@withContext cacheFile
        }

        return@withContext try {
            val bytes = callAzureTts(text)
            if (bytes != null && bytes.isNotEmpty()) {
                cacheFile.writeBytes(bytes)
                Log.d(TAG, "Áudio salvo: ${cacheFile.name} (${bytes.size} bytes)")
                cacheFile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro Azure TTS", e)
            null
        }
    }

    private fun callAzureTts(text: String): ByteArray? {
        val key    = BuildConfig.AZURE_TTS_KEY
        val region = BuildConfig.AZURE_TTS_REGION.ifBlank { "brazilsouth" }
        val url    = URL("https://$region.tts.speech.microsoft.com/cognitiveservices/v1")

        val ssml = """
            <speak version='1.0' xml:lang='pt-BR'>
                <voice xml:lang='pt-BR' xml:gender='Female' name='pt-BR-ThalitaNeural'>
                    ${text.escapeXml()}
                </voice>
            </speak>
        """.trimIndent()

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod  = "POST"
            connectTimeout = 10_000
            readTimeout    = 20_000
            setRequestProperty("Ocp-Apim-Subscription-Key", key)
            setRequestProperty("Content-Type", "application/ssml+xml")
            setRequestProperty("X-Microsoft-OutputFormat", "audio-16khz-128kbitrate-mono-mp3")
            setRequestProperty("User-Agent", "iautista")
            doOutput = true
            outputStream.use { it.write(ssml.toByteArray(Charsets.UTF_8)) }
        }

        return try {
            if (conn.responseCode == 200) {
                conn.inputStream.readBytes()
            } else {
                val err = conn.errorStream?.bufferedReader()?.readText() ?: ""
                Log.e(TAG, "Azure TTS HTTP ${conn.responseCode}: $err")
                null
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun String.escapeXml(): String = this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private fun cacheFileFor(text: String): File {
        val hash = MessageDigest.getInstance("MD5")
            .digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return File(cacheDir, "$hash.mp3")
    }

    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    companion object {
        private const val TAG = "AzureTts"
    }
}
