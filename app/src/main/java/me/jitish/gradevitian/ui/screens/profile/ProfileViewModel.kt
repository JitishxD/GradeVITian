package me.jitish.gradevitian.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.jitish.gradevitian.domain.model.UserProfile
import me.jitish.gradevitian.domain.repository.AuthRepository
import me.jitish.gradevitian.domain.util.Resource
import javax.inject.Inject

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        if (!authRepository.isAuthenticated) {
            _uiState.value = ProfileUiState(isAuthenticated = false)
            return
        }
        _uiState.value = _uiState.value.copy(isAuthenticated = true, isLoading = true)
        viewModelScope.launch {
            when (val result = authRepository.getCurrentProfile()) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    profile = result.data, isLoading = false
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    message = result.message, isLoading = false
                )
                is Resource.Loading -> {}
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = ProfileUiState(isAuthenticated = false)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

