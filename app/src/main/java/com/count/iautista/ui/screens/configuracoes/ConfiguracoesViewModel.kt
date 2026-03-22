package com.count.iautista.ui.screens.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.domain.model.AppTheme
import com.count.iautista.domain.model.ButtonSize
import com.count.iautista.domain.usecase.responsavel.GetUserPreferencesUseCase
import com.count.iautista.domain.usecase.responsavel.UpdatePreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfiguracoesUiState(
    val buttonSize: ButtonSize = ButtonSize.MEDIUM,
    val appTheme: AppTheme = AppTheme.LIGHT,
    val ttsEnabled: Boolean = true,
    val ttsRate: Float = 0.9f,
)

@HiltViewModel
class ConfiguracoesViewModel @Inject constructor(
    private val getPreferences: GetUserPreferencesUseCase,
    private val updatePreferences: UpdatePreferencesUseCase,
) : ViewModel() {

    val uiState: StateFlow<ConfiguracoesUiState> = getPreferences()
        .map { prefs ->
            ConfiguracoesUiState(
                buttonSize = prefs.buttonSize,
                appTheme   = prefs.appTheme,
                ttsEnabled = prefs.ttsEnabled,
                ttsRate    = prefs.ttsRate,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConfiguracoesUiState())

    fun setButtonSize(size: ButtonSize) {
        viewModelScope.launch { updatePreferences.setButtonSize(size) }
    }

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch { updatePreferences.setAppTheme(theme) }
    }

    fun setTtsEnabled(enabled: Boolean) {
        viewModelScope.launch { updatePreferences.setTtsEnabled(enabled) }
    }

    fun setTtsRate(rate: Float) {
        viewModelScope.launch { updatePreferences.setTtsRate(rate) }
    }
}
