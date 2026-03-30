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

// União de todos os textos de sugestão de todos os modos + necessidades.
// Um único getItemsByTexts carrega tudo o que a Home pode precisar exibir.
private val ALL_HOME_TEXTS = (
    UNIVERSAL_NEED_TEXTS +
    AppMode.entries.flatMap { mode -> mode.items.map { (_, label) -> label } }
).distinct()

/**
 * Estado de dados da tela inicial — NÃO inclui estado TTS.
 * TTS é coletado separadamente via [InicioViewModel.loadingLabel] e
 * [InicioViewModel.playingLabel] para que cliques em cards não disparem
 * recomputação de lookups, sugestões e seções de dados.
 */
data class InicioUiState(
    val profile: ChildProfile? = null,
    val greeting: String = "Olá!",
    val recentPhrases: List<PhraseHistory> = emptyList(),
    val mostUsedItems: List<CommunicationItem> = emptyList(),
    val routineNow: List<RoutineItem> = emptyList(),
    val routineNext: List<RoutineItem> = emptyList(),
    val appMode: AppMode = AppMode.CASA,
    val contextSuggestions: List<ContextSuggestion> = emptyList(),
    val emotionItems: List<CommunicationItem> = emptyList(),
    val universalNeedItems: List<CommunicationItem> = emptyList(),
)

/** Itens do banco usados para lookups na home — independente do TTS. */
private data class HomeItemsState(
    val emotions: List<CommunicationItem>,
    val needs: List<CommunicationItem>,
    val allHomeItems: List<CommunicationItem>,
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
    private val _stableMostUsed = MutableStateFlow<List<CommunicationItem>>(emptyList())

    // Flow compartilhado de dados gerais — 1 query em vez de 3.
    private val homeData: StateFlow<HomeData> = getHomeData().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeData(),
    )

    // Itens do banco necessários para a home — independente do TTS.
    // Só re-emite quando o banco muda, não quando o usuário clica.
    private val homeItemsState: StateFlow<HomeItemsState> = combine(
        getItemsByCategory(CATEGORY_SENTIMENTOS),
        getItemsByTexts(ALL_HOME_TEXTS),
    ) { emotions, rawHomeItems ->
        val needs = UNIVERSAL_NEED_TEXTS.mapNotNull { text -> rawHomeItems.find { it.text == text } }
        HomeItemsState(emotions, needs, rawHomeItems)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeItemsState(emptyList(), emptyList(), emptyList()),
    )

    // ── Estado TTS — coletado separadamente na UI ────────────────────────────
    // Separar do uiState garante que cliques em cards NÃO recomputem
    // emojiLookup, imageUriLookup, contextSuggestions nem nenhuma seção de dados.

    /** Label do item sendo sintetizado — UI exibe spinner apenas neste card. */
    val loadingLabel: StateFlow<String?> = combine(
        _activeLabel,
        ttsManager.isSynthesizing,
    ) { label, synthesizing -> if (synthesizing) label else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Label do item sendo reproduzido — UI exibe ícone de som apenas neste card. */
    val playingLabel: StateFlow<String?> = combine(
        _activeLabel,
        ttsManager.isSynthesizing,
        ttsManager.isPlaying,
    ) { label, synthesizing, playing -> if (playing && !synthesizing) label else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Limpa o label ativo quando ambas as fases terminam
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

    // uiState só re-emite por mudanças de dados reais:
    // perfil, saudação, modo, histórico, itens do banco, rotina.
    // NÃO re-emite por TTS → zero recomputação de lookups/sugestões por clique.
    val uiState: StateFlow<InicioUiState> = combine(
        homeData,
        getContextSnapshot().distinctUntilChangedBy { it.mode },
        _stableRecentPhrases,
        _stableMostUsed,
        homeItemsState,
    ) { data, snapshot, stableRecent, stableMostUsed, itemsState ->
        val allKnownItems = itemsState.allHomeItems + itemsState.emotions + stableMostUsed

        val emojiLookup = allKnownItems
            .filter { it.emoji.isNotBlank() }
            .associate { it.text.lowercase(Locale.ROOT) to it.emoji }

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
            emotionItems       = itemsState.emotions,
            universalNeedItems = itemsState.needs,
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

    fun speakSuggestion(text: String) {
        val state = uiState.value
        val knownItem = (state.emotionItems + state.universalNeedItems + state.mostUsedItems)
            .find { it.text.equals(text, ignoreCase = true) }
        if (knownItem != null) {
            speakItem(knownItem)
            return
        }
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
