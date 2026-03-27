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
    /** Sugestões contextuais prontas para exibição na seção "Para agora". */
    val contextSuggestions: List<ContextSuggestion> = emptyList(),
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

    val uiState: StateFlow<InicioUiState> = combine(
        getHomeData(),
        getContextSnapshot(),
    ) { data, snapshot ->
        InicioUiState(
            profile             = data.profile,
            greeting            = data.greeting,
            recentPhrases       = data.recentPhrases,
            mostUsedItems       = data.mostUsedItems,
            routineNow          = data.routineNow,
            routineNext         = data.routineNext,
            appMode             = snapshot.mode,
            contextSuggestions  = getContextualSuggestions(snapshot),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        // Estado inicial com itens padrão do modo CASA — evita flash de seção vazia
        // antes do primeiro emit do GetContextSnapshotUseCase.
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
        ttsManager.speak(text)
        viewModelScope.launch { saveQuickPhrase(text, uiState.value.appMode) }
    }

    fun speakItem(item: CommunicationItem) {
        ttsManager.speakOrPlayAudio(item.text, item.audioUri)
        viewModelScope.launch { trackUsage(item) }
    }
}
