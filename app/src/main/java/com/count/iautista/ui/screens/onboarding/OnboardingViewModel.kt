package com.count.iautista.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefsDataStore: UserPreferencesDataStore,
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            prefsDataStore.setOnboardingCompleted(true)
        }
    }
}
