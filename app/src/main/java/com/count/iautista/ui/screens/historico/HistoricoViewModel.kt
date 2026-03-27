package com.count.iautista.ui.screens.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.audio.TtsManager
import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.usecase.historico.GetPhrasesHistoryUseCase
import com.count.iautista.domain.usecase.historico.HistoryFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class HistoricoTab { AGORA, HOJE, SEMANA }

private fun HistoricoTab.toFilter() = when (this) {
    HistoricoTab.AGORA  -> HistoryFilter.RECENT
    HistoricoTab.HOJE   -> HistoryFilter.TODAY
    HistoricoTab.SEMANA -> HistoryFilter.WEEK
}

/** Uso por ambiente — exibido na seção "Por modo" como estatística. */
data class ModeUsage(val mode: AppMode, val count: Int)

data class HistoricoUiState(
    val selectedTab: HistoricoTab = HistoricoTab.AGORA,
    val phrases: List<PhraseHistory> = emptyList(),
    /** Top 6 frases por frequência no período selecionado. */
    val topPhrases: List<Pair<String, Int>> = emptyList(),
    /** Distribuição de uso por modo (só inclui modos com pelo menos 1 frase). */
    val modeUsage: List<ModeUsage> = emptyList(),
)

@HiltViewModel
class HistoricoViewModel @Inject constructor(
    private val getHistory: GetPhrasesHistoryUseCase,
    private val ttsManager: TtsManager,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(HistoricoTab.AGORA)

    val uiState: StateFlow<HistoricoUiState> = _selectedTab
        .flatMapLatest { tab ->
            getHistory(tab.toFilter()).map { all ->
                HistoricoUiState(
                    selectedTab  = tab,
                    phrases      = all,
                    topPhrases   = computeTopPhrases(all, limit = 6),
                    modeUsage    = computeModeUsage(all),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoricoUiState())

    fun selectTab(tab: HistoricoTab) {
        _selectedTab.value = tab
    }

    fun speakAgain(phrase: PhraseHistory) {
        ttsManager.speak(phrase.phraseText)
    }

    // ── Cálculos derivados ───────────────────────────────────────────────────

    private fun computeTopPhrases(phrases: List<PhraseHistory>, limit: Int): List<Pair<String, Int>> =
        phrases
            .groupBy { it.phraseText }
            .entries
            .sortedByDescending { it.value.size }
            .take(limit)
            .map { it.key to it.value.size }

    private fun computeModeUsage(phrases: List<PhraseHistory>): List<ModeUsage> =
        AppMode.entries
            .map { mode -> ModeUsage(mode, phrases.count { it.appMode == mode }) }
            .filter { it.count > 0 }
            .sortedByDescending { it.count }
}
