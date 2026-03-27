package com.count.iautista.ui.screens.responsavel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.auth.AuthRepository
import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.data.sync.FirestoreSyncService
import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.model.UserPreferences
import com.count.iautista.domain.usecase.responsavel.GetChildProfileUseCase
import com.count.iautista.domain.usecase.responsavel.GetUserPreferencesUseCase
import com.count.iautista.domain.usecase.responsavel.PinUseCase
import com.count.iautista.domain.usecase.responsavel.SaveChildProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResponsavelUiState(
    val profile: ChildProfile? = null,
    val preferences: UserPreferences = UserPreferences(),
    val isPinConfigured: Boolean = false,
    val isLoggedIn: Boolean = false,
)

@HiltViewModel
class ResponsavelViewModel @Inject constructor(
    private val getProfile: GetChildProfileUseCase,
    private val saveProfileUseCase: SaveChildProfileUseCase,
    private val getPreferences: GetUserPreferencesUseCase,
    private val pinUseCase: PinUseCase,
    private val authRepository: AuthRepository,
    private val dataStore: UserPreferencesDataStore,
    private val syncService: FirestoreSyncService,
) : ViewModel() {

    val uiState: StateFlow<ResponsavelUiState> = combine(
        getProfile(),
        getPreferences(),
    ) { profile, prefs ->
        ResponsavelUiState(
            profile          = profile,
            preferences      = prefs,
            isPinConfigured  = prefs.pinConfigured,
            isLoggedIn       = authRepository.isLoggedIn,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResponsavelUiState())

    fun validatePin(input: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(pinUseCase.validatePin(input))
        }
    }

    fun savePin(pin: String) {
        viewModelScope.launch { pinUseCase.setPin(pin) }
    }

    fun saveProfile(name: String, photoUri: String?) {
        viewModelScope.launch {
            saveProfileUseCase(
                ChildProfile(
                    id       = uiState.value.profile?.id ?: 0L,
                    name     = name,
                    photoUri = photoUri,
                )
            )
        }
    }

    fun signOut() {
        val uid = authRepository.currentUser?.uid
        viewModelScope.launch {
            // Faz backup antes de sair para não perder dados
            if (uid != null) runCatching { syncService.pushAll(uid) }
            authRepository.signOut()
            dataStore.setLoggedIn(false)
        }
    }

    fun pushBackup(onComplete: (success: Boolean) -> Unit) {
        val uid = authRepository.currentUser?.uid ?: run { onComplete(false); return }
        viewModelScope.launch {
            val result = runCatching { syncService.pushAll(uid) }
            onComplete(result.isSuccess)
        }
    }
}
