package com.count.iautista.domain.usecase.responsavel

import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.domain.model.AppTheme
import com.count.iautista.domain.model.ButtonSize
import javax.inject.Inject

/**
 * Agrupa todas as operações de escrita em preferências do responsável.
 * Cada função é uma operação pontual e suspend.
 */
class UpdatePreferencesUseCase @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
) {
    suspend fun setButtonSize(size: ButtonSize)         = dataStore.setButtonSize(size)
    suspend fun setAppTheme(theme: AppTheme)            = dataStore.setAppTheme(theme)
    suspend fun setTtsEnabled(enabled: Boolean)         = dataStore.setTtsEnabled(enabled)
    suspend fun setTtsRate(rate: Float)                 = dataStore.setTtsRate(rate)
    suspend fun setNotificationsEnabled(enabled: Boolean) = dataStore.setNotificationsEnabled(enabled)
}
