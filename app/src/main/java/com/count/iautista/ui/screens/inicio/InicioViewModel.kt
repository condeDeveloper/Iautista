package com.count.iautista.ui.screens.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.audio.TtsManager
import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.usecase.comunicar.TrackItemUsageUseCase
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
)

@HiltViewModel
class InicioViewModel @Inject constructor(
    private val getHomeData: GetHomeDataUseCase,
    private val saveQuickPhrase: SaveQuickPhraseUseCase,
    private val trackUsage: TrackItemUsageUseCase,
    private val ttsManager: TtsManager,
) : ViewModel() {

    val uiState: StateFlow<InicioUiState> = getHomeData()
        .map { data ->
            InicioUiState(
                profile      = data.profile,
                greeting     = data.greeting,
                recentPhrases = data.recentPhrases,
                mostUsedItems = data.mostUsedItems,
                routineNow   = data.routineNow,
                routineNext  = data.routineNext,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InicioUiState())

    /** Fala uma frase de atalho rápido (botões de necessidades/emoções). */
    fun speakPhrase(text: String) {
        ttsManager.speak(text)
        viewModelScope.launch { saveQuickPhrase(text) }
    }

    /** Fala e rastreia uso de um item da seção "Mais usadas". */
    fun speakItem(item: CommunicationItem) {
        ttsManager.speakOrPlayAudio(item.text, item.audioUri)
        viewModelScope.launch { trackUsage(item) }
    }
}
