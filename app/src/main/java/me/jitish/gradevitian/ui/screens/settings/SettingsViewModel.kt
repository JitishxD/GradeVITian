package me.jitish.gradevitian.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.jitish.gradevitian.domain.repository.PreferencesRepository
import javax.inject.Inject

data class SettingsUiState(
    val darkMode: Boolean = true,
    val dynamicColor: Boolean = false,
    val useMidTermFormula: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.observeDarkMode(),
        preferencesRepository.observeDynamicColor(),
        preferencesRepository.observeUseMidTermFormula()
    ) { darkMode, dynamicColor, useMidTermFormula ->
        SettingsUiState(
            darkMode = darkMode,
            dynamicColor = dynamicColor,
            useMidTermFormula = useMidTermFormula
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDarkMode(enabled) }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDynamicColor(enabled) }
    }

    fun toggleUseMidTermFormula(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setUseMidTermFormula(enabled) }
    }
}

