package com.count.iautista.ui.screens.rotina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.domain.usecase.rotina.DeleteRoutineItemUseCase
import com.count.iautista.domain.usecase.rotina.GetRoutineItemsUseCase
import com.count.iautista.domain.usecase.rotina.ResetDailyRoutineUseCase
import com.count.iautista.domain.usecase.rotina.UpdateRoutineStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RotinaUiState(
    val nowItems: List<RoutineItem> = emptyList(),
    val nextItems: List<RoutineItem> = emptyList(),
    val laterItems: List<RoutineItem> = emptyList(),
    val doneItems: List<RoutineItem> = emptyList(),
)

@HiltViewModel
class RotinaViewModel @Inject constructor(
    private val getRoutineItems: GetRoutineItemsUseCase,
    private val updateStatus: UpdateRoutineStatusUseCase,
    private val deleteItemUseCase: DeleteRoutineItemUseCase,
    private val resetRoutine: ResetDailyRoutineUseCase,
) : ViewModel() {

    val uiState: StateFlow<RotinaUiState> = getRoutineItems()
        .map { group ->
            RotinaUiState(
                nowItems   = group.now,
                nextItems  = group.next,
                laterItems = group.later,
                doneItems  = group.done,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RotinaUiState())

    fun markAsDone(item: RoutineItem) {
        viewModelScope.launch { updateStatus(item, RoutineStatus.DONE) }
    }

    fun markAsNow(item: RoutineItem) {
        viewModelScope.launch { updateStatus(item, RoutineStatus.NOW) }
    }

    fun resetDay() {
        viewModelScope.launch { resetRoutine() }
    }

    fun deleteItem(item: RoutineItem) {
        viewModelScope.launch { deleteItemUseCase(item) }
    }
}
