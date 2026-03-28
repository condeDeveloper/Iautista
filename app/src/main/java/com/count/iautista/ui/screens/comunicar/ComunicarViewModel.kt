package com.count.iautista.ui.screens.comunicar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.audio.TtsManager
import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.CommunicationCategory
import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.usecase.comunicar.BuildPhraseUseCase
import com.count.iautista.domain.usecase.comunicar.GetCategoriesUseCase
import com.count.iautista.domain.usecase.comunicar.GetFavoriteItemsUseCase
import com.count.iautista.domain.usecase.comunicar.GetItemsByCategoryUseCase
import com.count.iautista.domain.usecase.comunicar.ToggleFavoriteUseCase
import com.count.iautista.domain.usecase.comunicar.TrackItemUsageUseCase
import com.count.iautista.domain.usecase.historico.SaveQuickPhraseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComunicarUiState(
    val categories: List<CommunicationCategory> = emptyList(),
    val featuredCategoryIds: Set<Long> = emptySet(),
    val featuredCategories: List<CommunicationCategory> = emptyList(),
    val otherCategories: List<CommunicationCategory> = emptyList(),
    val favorites: List<CommunicationItem> = emptyList(),
    val items: List<CommunicationItem> = emptyList(),
    val selectedCategory: CommunicationCategory? = null,
    val phraseItems: List<CommunicationItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSpeaking: Boolean = false,
    val appMode: AppMode = AppMode.CASA,
)

@HiltViewModel
class ComunicarViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val getFavorites: GetFavoriteItemsUseCase,
    private val getItemsByCategory: GetItemsByCategoryUseCase,
    private val buildPhrase: BuildPhraseUseCase,
    private val trackUsage: TrackItemUsageUseCase,
    private val toggleFav: ToggleFavoriteUseCase,
    private val saveQuickPhrase: SaveQuickPhraseUseCase,
    private val ttsManager: TtsManager,
    private val prefsDataStore: UserPreferencesDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComunicarUiState())
    val uiState: StateFlow<ComunicarUiState> = _uiState.asStateFlow()

    private var categoryJob: Job? = null

    init {
        // Combina categorias + modo: ordena por prioridade e marca quais são destaque
        viewModelScope.launch {
            combine(
                getCategories(),
                prefsDataStore.preferences.map { it.appMode },
            ) { cats, mode ->
                val order = mode.pinnedCategoryIds
                val sorted = cats.sortedBy { cat ->
                    val idx = order.indexOf(cat.id)
                    if (idx >= 0) idx else (order.size + cat.order)
                }
                Triple(sorted, order.take(FEATURED_COUNT).toSet(), mode)
            }.collect { (sorted, featuredIds, mode) ->
                _uiState.update {
                    it.copy(
                        categories = sorted,
                        featuredCategoryIds = featuredIds,
                        featuredCategories = sorted.filter { cat -> cat.id in featuredIds },
                        otherCategories = sorted.filter { cat -> cat.id !in featuredIds },
                        appMode = mode,
                    )
                }
            }
        }
        viewModelScope.launch {
            getFavorites().collect { favs -> _uiState.update { it.copy(favorites = favs) } }
        }
        viewModelScope.launch {
            ttsManager.isSynthesizing.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
    }

    private companion object {
        const val FEATURED_COUNT = 4
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

    /** Fala uma sugestão contextual rápida e salva no histórico com o modo atual. */
    fun speakQuick(text: String) {
        ttsManager.speak(text)
        viewModelScope.launch { saveQuickPhrase(text, _uiState.value.appMode) }
    }

    fun toggleFavorite(item: CommunicationItem) {
        viewModelScope.launch { toggleFav(item) }
    }
}
