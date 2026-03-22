package com.count.iautista.ui.screens.responsavel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.domain.model.CommunicationCategory
import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.usecase.comunicar.DeleteItemUseCase
import com.count.iautista.domain.usecase.comunicar.GetCategoriesUseCase
import com.count.iautista.domain.usecase.comunicar.GetItemsByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GerenciarItensUiState(
    val categories: List<CommunicationCategory> = emptyList(),
    val expandedCategoryId: Long? = null,
    val categoryItems: List<CommunicationItem> = emptyList(),
    val isLoadingItems: Boolean = false,
    val deleteError: String? = null,
)

@HiltViewModel
class GerenciarItensViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val getItemsByCategory: GetItemsByCategoryUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GerenciarItensUiState())
    val uiState: StateFlow<GerenciarItensUiState> = _uiState.asStateFlow()

    private var itemsJob: Job? = null

    init {
        viewModelScope.launch {
            getCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun toggleCategory(categoryId: Long) {
        val current = _uiState.value.expandedCategoryId
        itemsJob?.cancel()
        if (current == categoryId) {
            _uiState.update { it.copy(expandedCategoryId = null, categoryItems = emptyList()) }
        } else {
            _uiState.update { it.copy(expandedCategoryId = categoryId, categoryItems = emptyList(), isLoadingItems = true) }
            itemsJob = viewModelScope.launch {
                getItemsByCategory(categoryId).collect { items ->
                    _uiState.update { it.copy(categoryItems = items, isLoadingItems = false) }
                }
            }
        }
    }

    fun deleteItem(item: CommunicationItem) {
        viewModelScope.launch {
            runCatching { deleteItemUseCase(item) }
                .onFailure { e ->
                    _uiState.update { it.copy(deleteError = e.message) }
                }
        }
    }

    fun clearDeleteError() {
        _uiState.update { it.copy(deleteError = null) }
    }
}
