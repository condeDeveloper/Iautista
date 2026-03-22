package com.count.iautista.domain.usecase.responsavel

import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserPreferencesUseCase @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
) {
    operator fun invoke(): Flow<UserPreferences> = dataStore.preferences
}
