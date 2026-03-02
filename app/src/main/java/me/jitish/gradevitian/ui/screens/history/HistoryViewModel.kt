package me.jitish.gradevitian.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.jitish.gradevitian.domain.model.AttendanceRecord
import me.jitish.gradevitian.domain.model.CgpaRecord
import me.jitish.gradevitian.domain.model.GpaRecord
import me.jitish.gradevitian.domain.repository.AuthRepository
import me.jitish.gradevitian.domain.repository.RecordsRepository
import me.jitish.gradevitian.domain.util.Resource
import javax.inject.Inject

data class HistoryUiState(
    val gpaRecords: List<GpaRecord> = emptyList(),
    val cgpaRecords: List<CgpaRecord> = emptyList(),
    val attendanceRecords: List<AttendanceRecord> = emptyList(),
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val recordsRepository: RecordsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadRecords()
    }

    private fun loadRecords() {
        val userId = authRepository.currentUserId
        if (userId == null) {
            _uiState.value = _uiState.value.copy(isAuthenticated = false)
            return
        }
        _uiState.value = _uiState.value.copy(isAuthenticated = true, isLoading = true)

        viewModelScope.launch {
            recordsRepository.observeGpaRecords(userId).collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.value = _uiState.value.copy(
                        gpaRecords = result.data, isLoading = false
                    )
                    is Resource.Error -> _uiState.value = _uiState.value.copy(
                        errorMessage = result.message, isLoading = false
                    )
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }

        viewModelScope.launch {
            recordsRepository.observeCgpaRecords(userId).collect { result ->
                if (result is Resource.Success) {
                    _uiState.value = _uiState.value.copy(cgpaRecords = result.data)
                }
            }
        }

        viewModelScope.launch {
            recordsRepository.observeAttendanceRecords(userId).collect { result ->
                if (result is Resource.Success) {
                    _uiState.value = _uiState.value.copy(attendanceRecords = result.data)
                }
            }
        }
    }

    fun deleteGpaRecord(recordId: String) {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            recordsRepository.deleteGpaRecord(userId, recordId)
        }
    }

    fun deleteCgpaRecord(recordId: String) {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            recordsRepository.deleteCgpaRecord(userId, recordId)
        }
    }

    fun deleteAttendanceRecord(recordId: String) {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            recordsRepository.deleteAttendanceRecord(userId, recordId)
        }
    }
}

