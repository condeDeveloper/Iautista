package com.count.iautista.ui.screens.responsavel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GerenciarPerfisUiState(
    val profiles: List<ChildProfile> = emptyList(),
    val activeProfileId: Long = 0L,
    val showAddDialog: Boolean = false,
    val editingProfile: ChildProfile? = null,
)

@HiltViewModel
class GerenciarPerfisViewModel @Inject constructor(
    private val repository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<GerenciarPerfisUiState> = combine(
        repository.getAllProfiles(),
        repository.getActiveProfileId(),
    ) { profiles, activeId ->
        GerenciarPerfisUiState(
            profiles        = profiles,
            activeProfileId = if (activeId != 0L) activeId else profiles.firstOrNull()?.id ?: 0L,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GerenciarPerfisUiState())

    fun showAddDialog()  { _showAddDialog.value = true }
    fun dismissDialog()  { _showAddDialog.value = false; _editingProfile.value = null }
    fun editProfile(profile: ChildProfile) { _editingProfile.value = profile }

    private val _showAddDialog   = MutableStateFlow(false)
    private val _editingProfile  = MutableStateFlow<ChildProfile?>(null)

    // Expose as part of uiState by merging in a second combine — simpler to use separate states
    val showAddDialog: StateFlow<Boolean>        = _showAddDialog.asStateFlow()
    val editingProfile: StateFlow<ChildProfile?> = _editingProfile.asStateFlow()

    fun addProfile(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.saveProfile(ChildProfile(name = name.trim()))
            // If this is the first profile, set it as active
            if (uiState.value.profiles.isEmpty()) {
                repository.setActiveProfileId(id)
            }
            dismissDialog()
        }
    }

    fun updateProfile(profile: ChildProfile, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateProfile(profile.copy(name = newName.trim()))
            dismissDialog()
        }
    }

    fun deleteProfile(profile: ChildProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            // If deleted profile was active, switch to first available
            if (uiState.value.activeProfileId == profile.id) {
                val remaining = uiState.value.profiles.filter { it.id != profile.id }
                repository.setActiveProfileId(remaining.firstOrNull()?.id ?: 0L)
            }
        }
    }

    fun selectProfile(profile: ChildProfile) {
        viewModelScope.launch {
            repository.setActiveProfileId(profile.id)
        }
    }
}
