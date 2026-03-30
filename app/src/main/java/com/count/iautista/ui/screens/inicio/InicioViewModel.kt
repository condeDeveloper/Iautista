package com.count.iautista.ui.screens.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.audio.TtsManager
import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.model.ContextSuggestion
import com.count.iautista.domain.model.HomeData
import com.count.iautista.domain.model.SuggestionSource
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.usecase.comunicar.GetItemByTextUseCase
import com.count.iautista.domain.usecase.comunicar.GetItemsByCategoryUseCase
import com.count.iautista.domain.usecase.comunicar.GetItemsByTextsUseCase
import com.count.iautista.domain.usecase.comunicar.TrackItemUsageUseCase
import com.count.iautista.domain.usecase.context.GetContextSnapshotUseCase
import com.count.iautista.domain.usecase.context.GetContextualSuggestionsUseCase
import com.count.iautista.domain.usecase.historico.SaveQuickPhraseUseCase
import com.count.iautista.domain.usecase.inicio.GetHomeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

// ID fixo da categoria "Sentimentos" no seed do banco
private const val CATEGORY_SENTIMENTOS = 2L

// Textos das necessidades universais — ordem fixa preservada ao mapear do banco
private val UNIVERSAL_NEED_TEXTS = listOf("Banheiro", "Água", "Dói", "Ajuda", "Cansado")

data class InicioUiState(
    val profile: ChildProfile? = null,
    val greeting: String = "Olá!",
    val recentPhrases: List<PhraseHistory> = emptyList(),
    val mostUsedItems: List<CommunicationItem> = emptyList(),
    val routineNow: List<RoutineItem> = emptyList(),
    val routineNext: List<RoutineItem> = emptyList(),
    val appMode: AppMode = AppMode.CASA,
    val contextSuggestions: List<ContextSuggestion> = emptyList(),
    /** Items do banco usados na seção "Como estou" — permite trackUsage correto. */
    val emotionItems: List<CommunicationItem> = emptyList(),
    /** Items do banco para "Necessidades" — mesma imagem ARASAAC em todo o app. */
    val universalNeedItems: List<CommunicationItem> = emptyList(),
    /** Label do item sendo baixado/sintetizado — exibe spinner. */
    val loadingLabel: String? = null,
    /** Label do item sendo reproduzido — exibe ícone de som. */
    val playingLabel: String? = null,
)

/** Estado interno combinado de TTS + emoções + necessidades para o combine aninhado. */
private data class TtsAndEmotionState(
    val label: String?,
    val synthesizing: Boolean,
    val playing: Boolean,
    val emotions: List<CommunicationItem>,
    val needs: List<CommunicationItem>,
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
    private val getItemsByCategory: GetItemsByCategoryUseCase,
    private val getItemsByTexts: GetItemsByTextsUseCase,
    private val getItemByText: GetItemByTextUseCase,
) : ViewModel() {

    /** Label do item ativo — limpo quando síntese e reprodução terminam. */
    private val _activeLabel = MutableStateFlow<String?>(null)

    // recentPhrases estabilizado: só adiciona ao front quando há frase genuinamente nova.
    private val _stableRecentPhrases = MutableStateFlow<List<PhraseHistory>>(emptyList())

    // mostUsedItems estabilizado: não reordena quando item já presente é clicado.
    // Só adiciona ao front quando um item genuinamente novo entra no top-8.
    private val _stableMostUsed = MutableStateFlow<List<CommunicationItem>>(emptyList())

    // Flow compartilhado — substitui as 3 chamadas independentes a getHomeData(),
    // reduzindo de 12 para 4 queries de DB ativas simultaneamente.
    private val homeData: StateFlow<HomeData> = getHomeData().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeData(),
    )

    init {
        // Limpa o label ativo quando ambas as fases (síntese + reprodução) terminam
        viewModelScope.launch {
            combine(ttsManager.isSynthesizing, ttsManager.isPlaying) { s, p -> s || p }
                .collect { active -> if (!active) _activeLabel.value = null }
        }

        viewModelScope.launch {
            homeData
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
                }
        }

        viewModelScope.launch {
            homeData
                .map { it.mostUsedItems }
                .collect { incoming ->
                    val current = _stableMostUsed.value
                    if (current.isEmpty()) {
                        _stableMostUsed.value = incoming
                        return@collect
                    }
                    val currentIds = current.map { it.id }.toSet()
                    val newItems = incoming.filter { it.id !in currentIds }
                    if (newItems.isNotEmpty()) {
                        _stableMostUsed.value = (newItems + current)
                            .distinctBy { it.id }
                            .take(8)
                    }
                }
        }
    }

    val uiState: StateFlow<InicioUiState> = combine(
        homeData,
        getContextSnapshot().distinctUntilChangedBy { it.mode },
        _stableRecentPhrases,
        _stableMostUsed,
        combine(
            _activeLabel,
            ttsManager.isSynthesizing,
            ttsManager.isPlaying,
            getItemsByCategory(CATEGORY_SENTIMENTOS),
            getItemsByTexts(UNIVERSAL_NEED_TEXTS),
        ) { label, synthesizing, playing, emotions, rawNeeds ->
            val needs = UNIVERSAL_NEED_TEXTS.mapNotNull { text -> rawNeeds.find { it.text == text } }
            TtsAndEmotionState(label, synthesizing, playing, emotions, needs)
        },
    ) { data, snapshot, stableRecent, stableMostUsed, ttsState ->
        val allKnownItems = ttsState.emotions + ttsState.needs + stableMostUsed

        // label→emoji: resolve emojis para sugestões do histórico fora de AppMode.items
        val emojiLookup = allKnownItems
            .filter { it.emoji.isNotBlank() }
            .associate { it.text.lowercase(Locale.ROOT) to it.emoji }

        // label→imageUri: garante o mesmo ícone ARASAAC em "Para agora" e demais seções
        val imageUriLookup = allKnownItems
            .filter { !it.imageUri.isNullOrBlank() }
            .associate { it.text.lowercase(Locale.ROOT) to it.imageUri!! }

        InicioUiState(
            profile            = data.profile,
            greeting           = data.greeting,
            recentPhrases      = stableRecent,
            mostUsedItems      = stableMostUsed,
            routineNow         = data.routineNow,
            routineNext        = data.routineNext,
            appMode            = snapshot.mode,
            contextSuggestions = getContextualSuggestions(snapshot, emojiLookup, imageUriLookup),
            emotionItems       = ttsState.emotions,
            universalNeedItems = ttsState.needs,
            loadingLabel       = if (ttsState.synthesizing) ttsState.label else null,
            playingLabel       = if (ttsState.playing && !ttsState.synthesizing) ttsState.label else null,
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

    /**
     * Fala uma sugestão contextual ou frase rápida.
     * Se o texto corresponder a um item do banco já carregado, chama [speakItem]
     * (incrementa usageCount → aparece em "Mais usadas"). Caso contrário, [speakPhrase].
     */
    fun speakSuggestion(text: String) {
        val state = uiState.value
        // Primeiro tenta nos itens já carregados em memória (sem IO)
        val knownItem = (state.emotionItems + state.universalNeedItems + state.mostUsedItems)
            .find { it.text.equals(text, ignoreCase = true) }
        if (knownItem != null) {
            speakItem(knownItem)
            return
        }
        // Fallback: busca no banco para qualquer categoria (Pessoas, Descanso, etc.)
        // Garante que "Papai", "Mamãe", "Dormir" etc. apareçam em "Mais usadas".
        viewModelScope.launch {
            val dbItem = getItemByText(text)
            if (dbItem != null) speakItem(dbItem) else speakPhrase(text)
        }
    }

    fun speakPhrase(text: String) {
        _activeLabel.value = text
        ttsManager.speak(text)
        viewModelScope.launch { saveQuickPhrase(text, uiState.value.appMode) }
    }

    fun speakItem(item: CommunicationItem) {
        _activeLabel.value = item.text
        ttsManager.speakOrPlayAudio(item.text, item.audioUri)
        viewModelScope.launch { trackUsage(item) }
    }
}
