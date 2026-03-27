package com.count.iautista.ui.screens.responsavel

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.domain.model.CommunicationCategory
import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.usecase.comunicar.GetCategoriesUseCase
import com.count.iautista.domain.usecase.comunicar.SaveCustomItemUseCase
import com.count.iautista.domain.usecase.comunicar.SaveItemResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AdicionarItemUiState(
    val text: String = "",
    val emoji: String = "",
    val selectedCategoryId: Long = 1L,
    val categories: List<CommunicationCategory> = emptyList(),
    val imageUri: String? = null,
    val audioUri: String? = null,
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val premiumLimitReached: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AdicionarItemViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCategories: GetCategoriesUseCase,
    private val saveCustomItem: SaveCustomItemUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdicionarItemUiState())
    val uiState: StateFlow<AdicionarItemUiState> = _uiState.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var pendingPhotoUri: android.net.Uri? = null

    init {
        viewModelScope.launch {
            getCategories().first().let { cats ->
                _uiState.update { it.copy(categories = cats, selectedCategoryId = cats.firstOrNull()?.id ?: 1L) }
            }
        }
    }

    // ── Campos de texto ──────────────────────────────────────────────────────

    fun updateText(text: String) = _uiState.update { it.copy(text = text, error = null) }
    fun updateEmoji(emoji: String) = _uiState.update { it.copy(emoji = emoji) }
    fun selectCategory(id: Long) = _uiState.update { it.copy(selectedCategoryId = id) }

    // ── Foto ─────────────────────────────────────────────────────────────────

    /** Cria um arquivo temporário e retorna a URI para a intent da câmera */
    fun createPhotoUri(): android.net.Uri {
        val dir = File(context.filesDir, "images").also { it.mkdirs() }
        val file = File(dir, "item_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            .also { pendingPhotoUri = it }
    }

    fun onPhotoCaptured(success: Boolean) {
        if (success) {
            _uiState.update { it.copy(imageUri = pendingPhotoUri?.toString()) }
        }
        pendingPhotoUri = null
    }

    fun onPhotoSelected(uri: android.net.Uri?) {
        _uiState.update { it.copy(imageUri = uri?.toString()) }
    }

    fun clearPhoto() = _uiState.update { it.copy(imageUri = null) }

    // ── Áudio ────────────────────────────────────────────────────────────────

    fun startRecording() {
        val dir = File(context.filesDir, "audio").also { it.mkdirs() }
        val file = File(dir, "audio_${System.currentTimeMillis()}.m4a")

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        _uiState.update { it.copy(isRecording = true, audioUri = file.absolutePath) }
    }

    fun stopRecording() {
        runCatching {
            mediaRecorder?.apply { stop(); release() }
        }
        mediaRecorder = null
        _uiState.update { it.copy(isRecording = false) }
    }

    fun playAudio() {
        val path = _uiState.value.audioUri ?: return
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
            setOnCompletionListener { _uiState.update { it.copy(isPlaying = false) } }
        }
        _uiState.update { it.copy(isPlaying = true) }
    }

    fun stopAudio() {
        mediaPlayer?.apply { stop(); release() }
        mediaPlayer = null
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun clearAudio() {
        stopAudio()
        _uiState.update { it.copy(audioUri = null) }
    }

    // ── Salvar ───────────────────────────────────────────────────────────────

    fun save() {
        val s = _uiState.value
        if (s.text.isBlank()) {
            _uiState.update { it.copy(error = "O nome do item é obrigatório") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val item = CommunicationItem(
                categoryId = s.selectedCategoryId,
                text       = s.text.trim(),
                emoji      = s.emoji.trim().ifBlank { "📌" },
                imageUri   = s.imageUri,
                audioUri   = s.audioUri,
                isDefault  = false,
            )
            when (saveCustomItem(item)) {
                is SaveItemResult.Success ->
                    _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
                is SaveItemResult.PremiumLimitReached ->
                    _uiState.update { it.copy(isSaving = false, premiumLimitReached = true) }
            }
        }
    }

    fun clearPremiumAlert() = _uiState.update { it.copy(premiumLimitReached = false) }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
        mediaPlayer?.release()
    }
}
