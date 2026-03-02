package me.jitish.gradevitian.ui.screens.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.jitish.gradevitian.domain.calculator.AttendanceCalculator
import me.jitish.gradevitian.domain.model.AttendanceRecord
import me.jitish.gradevitian.domain.repository.AuthRepository
import me.jitish.gradevitian.domain.repository.RecordsRepository
import me.jitish.gradevitian.domain.util.Resource
import javax.inject.Inject

data class AttendanceUiState(
    // Format 1 - Simple
    val simplePresent: String = "",
    val simpleAbsent: String = "",
    val simpleResultMessage: String? = null,
    val simplePercentage: Double? = null,
    val simpleIsError: Boolean = false,
    // Format 2 - Detailed
    val totalClasses: String = "",
    val detailedPresent: String = "",
    val detailedAbsent: String = "",
    val detailedResultMessage: String? = null,
    val detailedPercentage: Double? = null,
    val detailedIsError: Boolean = false,
    val saveMessage: String? = null
)

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceCalculator: AttendanceCalculator,
    private val authRepository: AuthRepository,
    private val recordsRepository: RecordsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    fun updateSimpleField(field: String, value: String) {
        _uiState.value = when (field) {
            "present" -> _uiState.value.copy(simplePresent = value, simpleResultMessage = null, simplePercentage = null)
            "absent" -> _uiState.value.copy(simpleAbsent = value, simpleResultMessage = null, simplePercentage = null)
            else -> _uiState.value
        }
    }

    fun updateDetailedField(field: String, value: String) {
        _uiState.value = when (field) {
            "total" -> _uiState.value.copy(totalClasses = value, detailedResultMessage = null, detailedPercentage = null)
            "present" -> _uiState.value.copy(detailedPresent = value, detailedResultMessage = null, detailedPercentage = null)
            "absent" -> _uiState.value.copy(detailedAbsent = value, detailedResultMessage = null, detailedPercentage = null)
            else -> _uiState.value
        }
    }

    fun calculateSimple() {
        val state = _uiState.value
        val result = attendanceCalculator.calculateSimple(
            classesPresent = state.simplePresent.toIntOrNull() ?: 0,
            classesAbsent = state.simpleAbsent.toIntOrNull() ?: 0
        )
        when (result) {
            is AttendanceCalculator.AttendanceValidation.Success -> {
                _uiState.value = state.copy(
                    simpleResultMessage = result.result.message,
                    simplePercentage = result.result.percentage,
                    simpleIsError = false
                )
            }
            is AttendanceCalculator.AttendanceValidation.Error -> {
                _uiState.value = state.copy(
                    simpleResultMessage = result.message,
                    simplePercentage = null,
                    simpleIsError = true
                )
            }
        }
    }

    fun calculateDetailed() {
        val state = _uiState.value
        val present = state.detailedPresent.toIntOrNull()
        val absent = state.detailedAbsent.toIntOrNull()
        val result = attendanceCalculator.calculateDetailed(
            totalClasses = state.totalClasses.toIntOrNull() ?: 0,
            classesPresent = if (state.detailedPresent.isNotBlank()) present else null,
            classesAbsent = if (state.detailedAbsent.isNotBlank()) absent else null
        )
        when (result) {
            is AttendanceCalculator.AttendanceValidation.Success -> {
                _uiState.value = state.copy(
                    detailedResultMessage = result.result.message,
                    detailedPercentage = result.result.percentage,
                    detailedIsError = false
                )
            }
            is AttendanceCalculator.AttendanceValidation.Error -> {
                _uiState.value = state.copy(
                    detailedResultMessage = result.message,
                    detailedPercentage = null,
                    detailedIsError = true
                )
            }
        }
    }

    fun resetSimple() {
        _uiState.value = _uiState.value.copy(
            simplePresent = "", simpleAbsent = "",
            simpleResultMessage = null, simplePercentage = null
        )
    }

    fun resetDetailed() {
        _uiState.value = _uiState.value.copy(
            totalClasses = "", detailedPresent = "", detailedAbsent = "",
            detailedResultMessage = null, detailedPercentage = null
        )
    }

    fun saveSimpleRecord() {
        val userId = authRepository.currentUserId ?: run {
            _uiState.value = _uiState.value.copy(saveMessage = "Sign in to save records")
            return
        }
        val percentage = _uiState.value.simplePercentage ?: run {
            _uiState.value = _uiState.value.copy(saveMessage = "Calculate first")
            return
        }
        viewModelScope.launch {
            val record = AttendanceRecord(
                attendancePercentage = percentage,
                classesPresent = _uiState.value.simplePresent.toIntOrNull() ?: 0,
                classesAbsent = _uiState.value.simpleAbsent.toIntOrNull() ?: 0,
                totalClasses = (_uiState.value.simplePresent.toIntOrNull() ?: 0) + (_uiState.value.simpleAbsent.toIntOrNull() ?: 0)
            )
            val result = recordsRepository.saveAttendanceRecord(userId, record)
            _uiState.value = _uiState.value.copy(
                saveMessage = if (result is Resource.Success) "Saved!" else "Failed to save"
            )
        }
    }

    fun clearSaveMessage() {
        _uiState.value = _uiState.value.copy(saveMessage = null)
    }
}

