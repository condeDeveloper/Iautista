package com.count.iautista.ui.screens.responsavel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.auth.AuthRepository
import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.data.sync.FirestoreSyncService
import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.domain.model.UserPreferences
import com.count.iautista.domain.usecase.historico.GetPhrasesHistoryUseCase
import com.count.iautista.domain.usecase.historico.HistoryFilter
import com.count.iautista.domain.usecase.responsavel.GetChildProfileUseCase
import com.count.iautista.domain.usecase.responsavel.GetUserPreferencesUseCase
import com.count.iautista.domain.usecase.responsavel.PinUseCase
import com.count.iautista.domain.usecase.responsavel.SaveChildProfileUseCase
import com.count.iautista.domain.usecase.rotina.GetRoutineItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayStats(
    val totalPhrases: Int = 0,
    val topMode: AppMode? = null,
    val routineCompleted: Int = 0,
    val routineTotal: Int = 0,
)

data class ResponsavelUiState(
    val profile: ChildProfile? = null,
    val preferences: UserPreferences = UserPreferences(),
    val isPinConfigured: Boolean = false,
    val isLoggedIn: Boolean = false,
    val todayStats: TodayStats = TodayStats(),
    val pinUnlocked: Boolean = false,
)

@HiltViewModel
class ResponsavelViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getProfile: GetChildProfileUseCase,
    private val saveProfileUseCase: SaveChildProfileUseCase,
    private val getPreferences: GetUserPreferencesUseCase,
    private val pinUseCase: PinUseCase,
    private val authRepository: AuthRepository,
    private val dataStore: UserPreferencesDataStore,
    private val syncService: FirestoreSyncService,
    private val getHistory: GetPhrasesHistoryUseCase,
    private val getRoutineItems: GetRoutineItemsUseCase,
) : ViewModel() {

    private val _pinUnlocked = savedStateHandle.getStateFlow("pin_unlocked", false)

    val uiState: StateFlow<ResponsavelUiState> = combine(
        getProfile(),
        getPreferences(),
        getHistory(HistoryFilter.TODAY),
        getRoutineItems(),
        _pinUnlocked,
    ) { profile, prefs, todayPhrases, routineGroup, pinUnlocked ->
        val topMode = todayPhrases
            .groupBy { it.appMode }
            .maxByOrNull { it.value.size }
            ?.key
        val routineCompleted = routineGroup.done.size
        val routineTotal = routineGroup.now.size + routineGroup.next.size +
                routineGroup.later.size + routineGroup.done.size
        ResponsavelUiState(
            profile         = profile,
            preferences     = prefs,
            isPinConfigured = prefs.pinConfigured,
            isLoggedIn      = authRepository.isLoggedIn,
            pinUnlocked     = pinUnlocked,
            todayStats      = TodayStats(
                totalPhrases     = todayPhrases.size,
                topMode          = topMode,
                routineCompleted = routineCompleted,
                routineTotal     = routineTotal,
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResponsavelUiState())

    fun validatePin(input: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val valid = pinUseCase.validatePin(input)
            if (valid) savedStateHandle["pin_unlocked"] = true
            onResult(valid)
        }
    }

    fun lockPin() { savedStateHandle["pin_unlocked"] = false }

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
