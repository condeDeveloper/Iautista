package com.count.iautista.ui.screens.rotina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.audio.TtsManager
import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.domain.usecase.rotina.AdvanceRoutineUseCase
import com.count.iautista.domain.usecase.rotina.DeleteRoutineItemUseCase
import com.count.iautista.domain.usecase.rotina.GetRoutineItemsUseCase
import com.count.iautista.domain.usecase.rotina.ResetDailyRoutineUseCase
import com.count.iautista.domain.usecase.rotina.SkipRoutineItemUseCase
import com.count.iautista.domain.usecase.rotina.UpdateRoutineStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class RotinaUiState(
    val nowItems: List<RoutineItem> = emptyList(),
    val nextItems: List<RoutineItem> = emptyList(),
    val laterItems: List<RoutineItem> = emptyList(),
    val doneItems: List<RoutineItem> = emptyList(),
    val appMode: AppMode = AppMode.CASA,
)

@HiltViewModel
class RotinaViewModel @Inject constructor(
    private val getRoutineItems: GetRoutineItemsUseCase,
    private val updateStatus: UpdateRoutineStatusUseCase,
    private val deleteItemUseCase: DeleteRoutineItemUseCase,
    private val resetRoutine: ResetDailyRoutineUseCase,
    private val advanceRoutine: AdvanceRoutineUseCase,
    private val skipRoutine: SkipRoutineItemUseCase,
    private val prefsDataStore: UserPreferencesDataStore,
    private val ttsManager: TtsManager,
) : ViewModel() {

    val uiState: StateFlow<RotinaUiState> = combine(
        getRoutineItems(),
        prefsDataStore.preferences.map { it.appMode },
    ) { group, mode ->
        RotinaUiState(
            nowItems   = group.now,
            nextItems  = group.next,
            laterItems = group.later,
            doneItems  = group.done,
            appMode    = mode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RotinaUiState())

    init {
        // Ao criar o ViewModel, verifica se é um novo dia e aplica o reset automático.
        viewModelScope.launch { checkAndAutoReset() }
    }

    /**
     * Verifica se a data do último reset é diferente de hoje.
     * Se for, reseta a rotina calculando NOW/NEXT com base no horário atual.
     */
    private suspend fun checkAndAutoReset() {
        val today = LocalDate.now().toString()
        val lastReset = prefsDataStore.getLastResetDate()
        if (lastReset != today) {
            resetRoutine.withSchedule(LocalTime.now().hour)
            prefsDataStore.setLastResetDate(today)
        }
    }

    /**
     * Marca o item como DONE e avança a cadeia automaticamente:
     * NEXT → NOW, primeiro LATER → NEXT.
     */
    fun markAsDone(item: RoutineItem) {
        viewModelScope.launch { advanceRoutine(item) }
    }

    /**
     * Pula o item atual: manda para o final da fila e avança a cadeia.
     */
    fun skipItem(item: RoutineItem) {
        viewModelScope.launch { skipRoutine(item) }
    }

    /**
     * Promoção manual: move um item de LATER ou NEXT direto para NOW.
     * Útil quando o responsável quer antecipar uma atividade.
     */
    fun markAsNow(item: RoutineItem) {
        viewModelScope.launch { updateStatus(item, RoutineStatus.NOW) }
    }

    /**
     * Reset manual pelo responsável (botão ↺).
     * Volta tudo para LATER e promove o primeiro item (por ordem) para NOW,
     * o segundo para NEXT — assim a rotina recomeça do início.
     */
    fun resetDay() {
        viewModelScope.launch {
            val allItems = (uiState.value.nowItems + uiState.value.nextItems +
                    uiState.value.laterItems + uiState.value.doneItems)
                .sortedBy { it.order }
            resetRoutine()
            if (allItems.isNotEmpty()) updateStatus(allItems[0], RoutineStatus.NOW)
            if (allItems.size > 1)     updateStatus(allItems[1], RoutineStatus.NEXT)
        }
    }

    fun deleteItem(item: RoutineItem) {
        viewModelScope.launch { deleteItemUseCase(item) }
    }

    /** Fala uma sugestão contextual da rotina via TTS. */
    fun speakHint(text: String) {
        ttsManager.speak(text)
    }
}
