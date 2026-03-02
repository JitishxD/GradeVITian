package me.jitish.gradevitian.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.jitish.gradevitian.domain.repository.AuthRepository
import me.jitish.gradevitian.domain.util.Resource
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isSignUp: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isAuthenticated: StateFlow<Boolean> = authRepository.observeAuthState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.isAuthenticated)

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun onDisplayNameChange(name: String) {
        _uiState.value = _uiState.value.copy(displayName = name, errorMessage = null)
    }

    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            isSignUp = !_uiState.value.isSignUp,
            errorMessage = null
        )
    }

    fun signInWithEmail() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please fill in all fields")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = authRepository.signInWithEmail(state.email.trim(), state.password)
            _uiState.value = when (result) {
                is Resource.Success -> _uiState.value.copy(isLoading = false, isSuccess = true)
                is Resource.Error -> _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is Resource.Loading -> _uiState.value.copy(isLoading = true)
            }
        }
    }

    fun signUpWithEmail() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank() || state.displayName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please fill in all fields")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(errorMessage = "Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = authRepository.signUpWithEmail(state.email.trim(), state.password, state.displayName.trim())
            _uiState.value = when (result) {
                is Resource.Success -> _uiState.value.copy(isLoading = false, isSuccess = true)
                is Resource.Error -> _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is Resource.Loading -> _uiState.value.copy(isLoading = true)
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.signInWithGoogle(idToken)
            _uiState.value = when (result) {
                is Resource.Success -> _uiState.value.copy(isLoading = false, isSuccess = true)
                is Resource.Error -> _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is Resource.Loading -> _uiState.value.copy(isLoading = true)
            }
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.email
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your email first")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = authRepository.sendPasswordResetEmail(email.trim())) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Password reset email sent!"
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
                is Resource.Loading -> {}
            }
        }
    }

    fun skipAuth() {
        // Allow using app without auth - mark as success
        _uiState.value = _uiState.value.copy(isSuccess = true)
    }
}

