package com.count.iautista.ui.screens.responsavel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.domain.usecase.rotina.DeleteRoutineItemUseCase
import com.count.iautista.domain.usecase.rotina.GetRoutineItemsUseCase
import com.count.iautista.domain.usecase.rotina.SaveRoutineItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val FREE_TIER_LIMIT = 5

data class GerenciarRotinaUiState(
    val items: List<RoutineItem> = emptyList(),
    val showAddDialog: Boolean = false,
    val showLimitDialog: Boolean = false,
)

@HiltViewModel
class GerenciarRotinaViewModel @Inject constructor(
    private val getRoutineItems: GetRoutineItemsUseCase,
    private val saveItem: SaveRoutineItemUseCase,
    private val deleteItem: DeleteRoutineItemUseCase,
) : ViewModel() {

    private val _showAddDialog   = MutableStateFlow(false)
    private val _showLimitDialog = MutableStateFlow(false)

    val uiState: StateFlow<GerenciarRotinaUiState> = getRoutineItems()
        .map { group ->
            (group.now + group.next + group.later + group.done).sortedBy { it.order }
        }
        .combine(_showAddDialog.combine(_showLimitDialog) { add, limit -> add to limit }) { items, (add, limit) ->
            GerenciarRotinaUiState(items = items, showAddDialog = add, showLimitDialog = limit)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GerenciarRotinaUiState())

    fun tryShowAddDialog() {
        if (uiState.value.items.size >= FREE_TIER_LIMIT) {
            _showLimitDialog.value = true
        } else {
            _showAddDialog.value = true
        }
    }
    fun dismissAddDialog()   { _showAddDialog.value = false }
    fun dismissLimitDialog() { _showLimitDialog.value = false }

    fun addItem(text: String, emoji: String, suggestedHour: Int?) {
        viewModelScope.launch {
            val items = uiState.value.items
            val nextOrder = if (items.isEmpty()) 0 else items.maxOf { it.order } + 1
            saveItem(
                RoutineItem(
                    text         = text.trim(),
                    emoji        = emoji.trim(),
                    status       = RoutineStatus.LATER,
                    order        = nextOrder,
                    suggestedHour = suggestedHour,
                )
            )
            _showAddDialog.value = false
        }
    }

    fun updateItem(item: RoutineItem, text: String, emoji: String, suggestedHour: Int?) {
        viewModelScope.launch {
            saveItem(item.copy(text = text.trim(), emoji = emoji.trim(), suggestedHour = suggestedHour))
        }
    }

    fun removeItem(item: RoutineItem) {
        viewModelScope.launch { deleteItem(item) }
    }

    /** Sobe o item uma posição — troca o `order` com o item anterior. */
    fun moveUp(item: RoutineItem) {
        viewModelScope.launch {
            val items = uiState.value.items
            val index = items.indexOfFirst { it.id == item.id }
            if (index <= 0) return@launch
            val prev = items[index - 1]
            saveItem(item.copy(order = prev.order))
            saveItem(prev.copy(order = item.order))
        }
    }

    /** Desce o item uma posição — troca o `order` com o item seguinte. */
    fun moveDown(item: RoutineItem) {
        viewModelScope.launch {
            val items = uiState.value.items
            val index = items.indexOfFirst { it.id == item.id }
            if (index < 0 || index >= items.size - 1) return@launch
            val next = items[index + 1]
            saveItem(item.copy(order = next.order))
            saveItem(next.copy(order = item.order))
        }
    }
}
