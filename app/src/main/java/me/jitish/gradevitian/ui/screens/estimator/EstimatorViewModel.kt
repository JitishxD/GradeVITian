package me.jitish.gradevitian.ui.screens.estimator

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.jitish.gradevitian.domain.calculator.CgpaEstimator
import javax.inject.Inject

data class EstimatorUiState(
    val desiredCgpa: String = "",
    val currentCgpa: String = "",
    val completedCredits: String = "",
    val newCredits: String = "",
    val resultTitle: String? = null,
    val resultSubtitle: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class EstimatorViewModel @Inject constructor(
    private val cgpaEstimator: CgpaEstimator
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstimatorUiState())
    val uiState: StateFlow<EstimatorUiState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.value = when (field) {
            "desired" -> _uiState.value.copy(desiredCgpa = value, resultTitle = null)
            "current" -> _uiState.value.copy(currentCgpa = value, resultTitle = null)
            "completed" -> _uiState.value.copy(completedCredits = value, resultTitle = null)
            "new" -> _uiState.value.copy(newCredits = value, resultTitle = null)
            else -> _uiState.value
        }
    }

    fun calculate() {
        val state = _uiState.value
        val result = cgpaEstimator.estimate(
            desiredCgpa = state.desiredCgpa.toDoubleOrNull() ?: 0.0,
            currentCgpa = state.currentCgpa.toDoubleOrNull() ?: 0.0,
            completedCredits = state.completedCredits.toIntOrNull() ?: 0,
            newCredits = state.newCredits.toIntOrNull() ?: 0
        )
        when (result) {
            is CgpaEstimator.EstimationValidation.Success -> {
                _uiState.value = state.copy(
                    resultTitle = "Your minimum GPA in the next sem should be ${result.result.requiredGpa}.",
                    resultSubtitle = result.result.message,
                    isError = false
                )
            }
            is CgpaEstimator.EstimationValidation.Error -> {
                _uiState.value = state.copy(
                    resultTitle = result.message,
                    resultSubtitle = result.detail,
                    isError = true
                )
            }
        }
    }

    fun reset() {
        _uiState.value = EstimatorUiState()
    }
}

