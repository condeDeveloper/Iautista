package com.count.iautista.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefsDataStore: UserPreferencesDataStore,
) : ViewModel() {

    fun completeOnboarding() {
        // NonCancellable garante que a escrita no DataStore termina mesmo se o
        // NavBackStackEntry for destruído (popUpTo inclusive) antes do fim da coroutine.
        viewModelScope.launch {
            withContext(NonCancellable) {
                prefsDataStore.setOnboardingCompleted(true)
            }
        }
    }
}
