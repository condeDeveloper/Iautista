package com.count.iautista.ui.screens.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.audio.TtsManager
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.usecase.historico.GetPhrasesHistoryUseCase
import com.count.iautista.domain.usecase.historico.HistoryFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/** Enum de UI — mapeado para HistoryFilter antes de chamar o use case. */
enum class HistoricoTab { RECENTES, HOJE, SEMANA }

private fun HistoricoTab.toFilter() = when (this) {
    HistoricoTab.RECENTES -> HistoryFilter.RECENT
    HistoricoTab.HOJE     -> HistoryFilter.TODAY
    HistoricoTab.SEMANA   -> HistoryFilter.WEEK
}

data class HistoricoUiState(
    val phrases: List<PhraseHistory> = emptyList(),
    val selectedTab: HistoricoTab = HistoricoTab.RECENTES,
)

@HiltViewModel
class HistoricoViewModel @Inject constructor(
    private val getHistory: GetPhrasesHistoryUseCase,
    private val ttsManager: TtsManager,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(HistoricoTab.RECENTES)

    val uiState: StateFlow<HistoricoUiState> = _selectedTab
        .flatMapLatest { tab ->
            getHistory(tab.toFilter()).map { phrases ->
                HistoricoUiState(phrases = phrases, selectedTab = tab)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoricoUiState())

    fun selectTab(tab: HistoricoTab) {
        _selectedTab.value = tab
    }

    fun speakAgain(phrase: PhraseHistory) {
        ttsManager.speak(phrase.phraseText)
    }
}
