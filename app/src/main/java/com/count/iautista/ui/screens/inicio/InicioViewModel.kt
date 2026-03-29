package com.count.iautista.ui.screens.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.audio.TtsManager
import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.model.ContextSuggestion
import com.count.iautista.domain.model.SuggestionSource
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.usecase.comunicar.TrackItemUsageUseCase
import com.count.iautista.domain.usecase.context.GetContextSnapshotUseCase
import com.count.iautista.domain.usecase.context.GetContextualSuggestionsUseCase
import com.count.iautista.domain.usecase.historico.SaveQuickPhraseUseCase
import com.count.iautista.domain.usecase.inicio.GetHomeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InicioUiState(
    val profile: ChildProfile? = null,
    val greeting: String = "Olá!",
    val recentPhrases: List<PhraseHistory> = emptyList(),
    val mostUsedItems: List<CommunicationItem> = emptyList(),
    val routineNow: List<RoutineItem> = emptyList(),
    val routineNext: List<RoutineItem> = emptyList(),
    val appMode: AppMode = AppMode.CASA,
    val contextSuggestions: List<ContextSuggestion> = emptyList(),
    /** Label da frase sendo sintetizada pelo TTS (null = nenhuma). */
    val speakingLabel: String? = null,
)

@HiltViewModel
class InicioViewModel @Inject constructor(
    private val getHomeData: GetHomeDataUseCase,
    private val saveQuickPhrase: SaveQuickPhraseUseCase,
    private val trackUsage: TrackItemUsageUseCase,
    private val ttsManager: TtsManager,
    private val prefsDataStore: UserPreferencesDataStore,
    private val getContextSnapshot: GetContextSnapshotUseCase,
    private val getContextualSuggestions: GetContextualSuggestionsUseCase,
) : ViewModel() {

    // Label da frase atualmente em síntese — limpo quando isSynthesizing volta a false.
    private val _speakingLabel = MutableStateFlow<String?>(null)

    // recentPhrases estabilizado: só adiciona ao front quando há frase genuinamente nova.
    // Evita reordenação ao clicar em "falar novamente".
    private val _stableRecentPhrases = MutableStateFlow<List<PhraseHistory>>(emptyList())

    init {
        // Sincroniza _speakingLabel com o estado de síntese do TTS.
        viewModelScope.launch {
            ttsManager.isSynthesizing.collect { synthesizing ->
                if (!synthesizing) _speakingLabel.value = null
            }
        }

        // Mantém _stableRecentPhrases atualizado sem reordenar itens já exibidos.
        viewModelScope.launch {
            getHomeData()
                .map { it.recentPhrases.distinctBy { p -> p.phraseText } }
                .collect { incoming ->
                    val current = _stableRecentPhrases.value
                    if (current.isEmpty()) {
                        _stableRecentPhrases.value = incoming.take(6)
                        return@collect
                    }
                    val currentTexts = current.map { it.phraseText }.toSet()
                    val newItems = incoming.filter { it.phraseText !in currentTexts }
                    if (newItems.isNotEmpty()) {
                        _stableRecentPhrases.value = (newItems + current)
                            .distinctBy { it.phraseText }
                            .take(6)
                    }
                    // Sem frases novas → mantém ordem atual (evita reordenação ao clicar)
                }
        }
    }

    val uiState: StateFlow<InicioUiState> = combine(
        getHomeData(),
        // distinctUntilChangedBy { mode }: sugestões só reordenam quando o modo muda,
        // não a cada frase falada — evita a dança de itens ao clicar.
        getContextSnapshot().distinctUntilChangedBy { it.mode },
        _stableRecentPhrases,
        _speakingLabel,
    ) { data, snapshot, stableRecent, speakingLabel ->
        InicioUiState(
            profile             = data.profile,
            greeting            = data.greeting,
            recentPhrases       = stableRecent,
            mostUsedItems       = data.mostUsedItems,
            routineNow          = data.routineNow,
            routineNext         = data.routineNext,
            appMode             = snapshot.mode,
            contextSuggestions  = getContextualSuggestions(snapshot),
            speakingLabel       = speakingLabel,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InicioUiState(
            contextSuggestions = AppMode.CASA.items.map { (emoji, label) ->
                ContextSuggestion(emoji, label, SuggestionSource.MODE_DEFAULT)
            },
        ),
    )

    fun setMode(mode: AppMode) {
        viewModelScope.launch { prefsDataStore.setAppMode(mode) }
    }

    fun speakPhrase(text: String) {
        _speakingLabel.value = text
        ttsManager.speak(text)
        viewModelScope.launch { saveQuickPhrase(text, uiState.value.appMode) }
    }

    fun speakItem(item: CommunicationItem) {
        _speakingLabel.value = item.text
        ttsManager.speakOrPlayAudio(item.text, item.audioUri)
        viewModelScope.launch { trackUsage(item) }
    }
}
