package com.count.iautista.ui.screens.comunicar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.audio.TtsManager
import com.count.iautista.domain.model.CommunicationCategory
import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.usecase.comunicar.BuildPhraseUseCase
import com.count.iautista.domain.usecase.comunicar.GetCategoriesUseCase
import com.count.iautista.domain.usecase.comunicar.GetFavoriteItemsUseCase
import com.count.iautista.domain.usecase.comunicar.GetItemsByCategoryUseCase
import com.count.iautista.domain.usecase.comunicar.ToggleFavoriteUseCase
import com.count.iautista.domain.usecase.comunicar.TrackItemUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComunicarUiState(
    val categories: List<CommunicationCategory> = emptyList(),
    val favorites: List<CommunicationItem> = emptyList(),
    val items: List<CommunicationItem> = emptyList(),
    val selectedCategory: CommunicationCategory? = null,
    val phraseItems: List<CommunicationItem> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class ComunicarViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val getFavorites: GetFavoriteItemsUseCase,
    private val getItemsByCategory: GetItemsByCategoryUseCase,
    private val buildPhrase: BuildPhraseUseCase,
    private val trackUsage: TrackItemUsageUseCase,
    private val toggleFav: ToggleFavoriteUseCase,
    private val ttsManager: TtsManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComunicarUiState())
    val uiState: StateFlow<ComunicarUiState> = _uiState.asStateFlow()

    private var categoryJob: Job? = null

    init {
        viewModelScope.launch {
            getCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
        viewModelScope.launch {
            getFavorites().collect { favs ->
                _uiState.update { it.copy(favorites = favs) }
            }
        }
    }

    fun loadCategory(categoryId: Long) {
        categoryJob?.cancel()
        categoryJob = viewModelScope.launch {
            val cat = _uiState.value.categories.find { it.id == categoryId }
                ?: getCategories().first().find { it.id == categoryId }
            _uiState.update { it.copy(selectedCategory = cat) }
            getItemsByCategory(categoryId).collect { items ->
                _uiState.update { it.copy(items = items) }
            }
        }
    }

    fun addItemToPhrase(item: CommunicationItem) {
        _uiState.update { it.copy(phraseItems = it.phraseItems + item) }
    }

    fun removeItemFromPhrase(item: CommunicationItem) {
        _uiState.update { it.copy(phraseItems = it.phraseItems - item) }
    }

    fun clearPhrase() {
        _uiState.update { it.copy(phraseItems = emptyList()) }
    }

    fun speakPhrase() {
        val items = _uiState.value.phraseItems
        if (items.isEmpty()) return
        viewModelScope.launch {
            val text = buildPhrase(items)
            ttsManager.speak(text)
            clearPhrase()
        }
    }

    fun speakItem(item: CommunicationItem) {
        ttsManager.speakOrPlayAudio(item.text, item.audioUri)
        viewModelScope.launch { trackUsage(item) }
    }

    fun toggleFavorite(item: CommunicationItem) {
        viewModelScope.launch { toggleFav(item) }
    }
}
