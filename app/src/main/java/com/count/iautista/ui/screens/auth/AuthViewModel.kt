package com.count.iautista.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.auth.AuthRepository
import com.count.iautista.data.auth.AuthResult
import com.count.iautista.data.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesDataStore: UserPreferencesDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(isLoggedIn = authRepository.isLoggedIn))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) =
        _uiState.update { it.copy(email = email.trim(), error = null) }

    fun updatePassword(password: String) =
        _uiState.update { it.copy(password = password, error = null) }

    fun updateConfirmPassword(confirm: String) =
        _uiState.update { it.copy(confirmPassword = confirm, error = null) }

    fun signIn() {
        val s = _uiState.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _uiState.update { it.copy(error = "Preencha e-mail e senha") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signIn(s.email, s.password)) {
                is AuthResult.Success -> {
                    preferencesDataStore.setLoggedIn(true)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun signUp() {
        val s = _uiState.value
        when {
            s.email.isBlank() || s.password.isBlank() -> {
                _uiState.update { it.copy(error = "Preencha todos os campos") }
                return
            }
            s.password.length < 6 -> {
                _uiState.update { it.copy(error = "A senha deve ter pelo menos 6 caracteres") }
                return
            }
            s.password != s.confirmPassword -> {
                _uiState.update { it.copy(error = "As senhas não coincidem") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signUp(s.email, s.password)) {
                is AuthResult.Success -> {
                    preferencesDataStore.setLoggedIn(true)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
}
