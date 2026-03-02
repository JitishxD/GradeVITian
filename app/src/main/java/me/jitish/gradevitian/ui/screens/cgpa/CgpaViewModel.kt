package me.jitish.gradevitian.ui.screens.cgpa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.jitish.gradevitian.domain.calculator.CgpaCalculator
import me.jitish.gradevitian.domain.model.CgpaRecord
import me.jitish.gradevitian.domain.model.SemesterEntry
import me.jitish.gradevitian.domain.repository.AuthRepository
import me.jitish.gradevitian.domain.repository.RecordsRepository
import me.jitish.gradevitian.domain.util.Resource
import me.jitish.gradevitian.ui.navigation.PendingRecordHolder
import javax.inject.Inject

data class CgpaUiState(
    val semesters: List<SemesterEntry> = listOf(SemesterEntry(semesterNumber = 1)),
    // String maps to keep raw text input (avoids Double→String ".0" issue)
    val semCreditsText: Map<Int, String> = emptyMap(),
    val semGpaText: Map<Int, String> = emptyMap(),
    val resultTitle: String? = null,
    val resultSubtitle: String? = null,
    val isError: Boolean = false,
    // Instant CGPA fields
    val currentCgpa: String = "",
    val completedCredits: String = "",
    val currentSemGpa: String = "",
    val currentSemCredits: String = "",
    val instantResultTitle: String? = null,
    val instantResultSubtitle: String? = null,
    val instantIsError: Boolean = false,
    val saveMessage: String? = null
)

@HiltViewModel
class CgpaViewModel @Inject constructor(
    private val cgpaCalculator: CgpaCalculator,
    private val authRepository: AuthRepository,
    private val recordsRepository: RecordsRepository,
    private val pendingRecordHolder: PendingRecordHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(CgpaUiState())
    val uiState: StateFlow<CgpaUiState> = _uiState.asStateFlow()

    init {
        pendingRecordHolder.pendingCgpaRecord?.let { record ->
            pendingRecordHolder.pendingCgpaRecord = null
            loadFromRecord(record)
        }
    }

    fun updateSemCredits(semNumber: Int, text: String) {
        val credits = text.toIntOrNull() ?: 0
        val semesters = _uiState.value.semesters.map {
            if (it.semesterNumber == semNumber) it.copy(credits = credits) else it
        }
        val textMap = _uiState.value.semCreditsText.toMutableMap()
        textMap[semNumber] = text
        _uiState.value = _uiState.value.copy(semesters = semesters, semCreditsText = textMap, resultTitle = null)
    }

    fun updateSemGpa(semNumber: Int, text: String) {
        val gpa = text.toDoubleOrNull() ?: 0.0
        val semesters = _uiState.value.semesters.map {
            if (it.semesterNumber == semNumber) it.copy(gpa = gpa) else it
        }
        val textMap = _uiState.value.semGpaText.toMutableMap()
        textMap[semNumber] = text
        _uiState.value = _uiState.value.copy(semesters = semesters, semGpaText = textMap, resultTitle = null)
    }

    fun calculateSemesterWise() {
        when (val result = cgpaCalculator.calculate(_uiState.value.semesters)) {
            is CgpaCalculator.CgpaValidation.Success -> {
                _uiState.value = _uiState.value.copy(
                    resultTitle = "Your CGPA is ${result.result.cgpa}",
                    resultSubtitle = result.result.message,
                    isError = false
                )
            }
            is CgpaCalculator.CgpaValidation.Error -> {
                _uiState.value = _uiState.value.copy(
                    resultTitle = result.message,
                    resultSubtitle = result.detail,
                    isError = true
                )
            }
        }
    }

    fun updateInstantField(field: String, value: String) {
        _uiState.value = when (field) {
            "cgpa" -> _uiState.value.copy(currentCgpa = value, instantResultTitle = null)
            "completedCredits" -> _uiState.value.copy(completedCredits = value, instantResultTitle = null)
            "semGpa" -> _uiState.value.copy(currentSemGpa = value, instantResultTitle = null)
            "semCredits" -> _uiState.value.copy(currentSemCredits = value, instantResultTitle = null)
            else -> _uiState.value
        }
    }

    fun calculateInstant() {
        val state = _uiState.value
        val result = cgpaCalculator.calculateInstantCgpa(
            currentCgpa = state.currentCgpa.toDoubleOrNull() ?: 0.0,
            completedCredits = state.completedCredits.toIntOrNull() ?: 0,
            currentSemGpa = state.currentSemGpa.toDoubleOrNull() ?: 0.0,
            currentSemCredits = state.currentSemCredits.toIntOrNull() ?: 0
        )
        when (result) {
            is CgpaCalculator.CgpaValidation.Success -> {
                _uiState.value = state.copy(
                    instantResultTitle = "Your CGPA is ${result.result.cgpa}",
                    instantResultSubtitle = result.result.message,
                    instantIsError = false
                )
            }
            is CgpaCalculator.CgpaValidation.Error -> {
                _uiState.value = state.copy(
                    instantResultTitle = result.message,
                    instantResultSubtitle = result.detail,
                    instantIsError = true
                )
            }
        }
    }

    fun addSemester() {
        val semesters = _uiState.value.semesters
        val nextNum = (semesters.maxOfOrNull { it.semesterNumber } ?: 0) + 1
        _uiState.value = _uiState.value.copy(semesters = semesters + SemesterEntry(semesterNumber = nextNum))
    }

    fun removeSemester(semNumber: Int) {
        if (_uiState.value.semesters.size <= 1) return
        val semesters = _uiState.value.semesters.filter { it.semesterNumber != semNumber }
        val textCredits = _uiState.value.semCreditsText.toMutableMap().apply { remove(semNumber) }
        val textGpa = _uiState.value.semGpaText.toMutableMap().apply { remove(semNumber) }
        _uiState.value = _uiState.value.copy(
            semesters = semesters,
            semCreditsText = textCredits,
            semGpaText = textGpa
        )
    }

    fun resetSemesterWise() {
        _uiState.value = _uiState.value.copy(
            semesters = listOf(SemesterEntry(semesterNumber = 1)),
            semCreditsText = emptyMap(),
            semGpaText = emptyMap(),
            resultTitle = null,
            resultSubtitle = null
        )
    }

    fun loadFromRecord(record: CgpaRecord) {
        val semesters = if (record.semesters.isNotEmpty()) {
            record.semesters.mapIndexed { index, sem -> sem.copy(semesterNumber = index + 1) }
        } else {
            listOf(SemesterEntry(semesterNumber = 1))
        }
        val creditsText = semesters.filter { it.credits > 0 }
            .associate { it.semesterNumber to it.credits.toString() }
        val gpaText = semesters.filter { it.gpa > 0.0 }
            .associate { it.semesterNumber to it.gpa.toBigDecimal().stripTrailingZeros().toPlainString() }
        _uiState.value = _uiState.value.copy(
            semesters = semesters,
            semCreditsText = creditsText,
            semGpaText = gpaText
        )
        calculateSemesterWise()
    }

    fun resetInstant() {
        _uiState.value = _uiState.value.copy(
            currentCgpa = "", completedCredits = "",
            currentSemGpa = "", currentSemCredits = "",
            instantResultTitle = null, instantResultSubtitle = null
        )
    }

    fun saveRecord() {
        val userId = authRepository.currentUserId ?: run {
            _uiState.value = _uiState.value.copy(saveMessage = "Sign in to save records")
            return
        }
        val result = cgpaCalculator.calculate(_uiState.value.semesters)
        if (result !is CgpaCalculator.CgpaValidation.Success) {
            _uiState.value = _uiState.value.copy(saveMessage = "Calculate first")
            return
        }
        viewModelScope.launch {
            val record = CgpaRecord(
                semesters = _uiState.value.semesters.filter { it.credits > 0 && it.gpa > 0 },
                cgpa = result.result.cgpa,
                totalCredits = result.result.totalCredits
            )
            val saveResult = recordsRepository.saveCgpaRecord(userId, record)
            _uiState.value = _uiState.value.copy(
                saveMessage = if (saveResult is Resource.Success) "Saved!" else "Failed to save"
            )
        }
    }

    fun clearSaveMessage() {
        _uiState.value = _uiState.value.copy(saveMessage = null)
    }
}

