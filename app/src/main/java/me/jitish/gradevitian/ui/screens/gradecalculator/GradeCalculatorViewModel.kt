package me.jitish.gradevitian.ui.screens.gradecalculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.jitish.gradevitian.domain.calculator.RelativeGradeCalculator
import me.jitish.gradevitian.domain.repository.PreferencesRepository
import java.util.Locale
import javax.inject.Inject

enum class GradeCalcMode {
    CLASS_MARKS,
    DIRECT_MU_SIGMA
}

data class GradeCalculatorUiState(
    val mode: GradeCalcMode = GradeCalcMode.CLASS_MARKS,
    val useMidTermFormula: Boolean = false,
    val cat1: String = "",
    val cat2: String = "",
    val midTerm: String = "",
    val tee: String = "",
    val attendance: String = "",
    val otherAssessment: String = "",
    val classTotalsRaw: String = "",
    val meanInput: String = "",
    val sigmaInput: String = "",
    val resultTitle: String? = null,
    val resultSubtitle: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class GradeCalculatorViewModel @Inject constructor(
    private val calculator: RelativeGradeCalculator,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradeCalculatorUiState())
    val uiState: StateFlow<GradeCalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.observeUseMidTermFormula().collect { useMidTermFormula ->
                _uiState.value = _uiState.value.copy(
                    useMidTermFormula = useMidTermFormula,
                    resultTitle = null,
                    resultSubtitle = null,
                    isError = false
                )
            }
        }
    }

    fun setMode(mode: GradeCalcMode) {
        _uiState.value = _uiState.value.copy(
            mode = mode,
            resultTitle = null,
            resultSubtitle = null,
            isError = false
        )
    }

    fun updateField(field: String, value: String) {
        _uiState.value = when (field) {
            "cat1" -> _uiState.value.copy(cat1 = value, resultTitle = null)
            "cat2" -> _uiState.value.copy(cat2 = value, resultTitle = null)
            "midTerm" -> _uiState.value.copy(midTerm = value, resultTitle = null)
            "tee" -> _uiState.value.copy(tee = value, resultTitle = null)
            "attendance" -> _uiState.value.copy(attendance = value, resultTitle = null)
            "otherAssessment" -> _uiState.value.copy(otherAssessment = value, resultTitle = null)
            "classTotalsRaw" -> _uiState.value.copy(classTotalsRaw = value, resultTitle = null)
            "meanInput" -> _uiState.value.copy(meanInput = value, resultTitle = null)
            "sigmaInput" -> _uiState.value.copy(sigmaInput = value, resultTitle = null)
            else -> _uiState.value
        }
    }

    fun calculate() {
        val s = _uiState.value
        val cat1 = s.cat1.toDoubleOrNull()
        val cat2 = s.cat2.toDoubleOrNull()
        val midTerm = s.midTerm.toDoubleOrNull()
        val tee = s.tee.toDoubleOrNull()
        val attendance = s.attendance.toDoubleOrNull()
        val otherAssessment = s.otherAssessment.toDoubleOrNull()

        val (mean, sigma) = when (s.mode) {
            GradeCalcMode.CLASS_MARKS -> {
                val parsed = parseClassTotals(s.classTotalsRaw)
                if (parsed.isEmpty()) {
                    showError("Enter class total marks as comma-separated values.")
                    return
                }
                if (parsed.any { it < 0.0 }) {
                    showError("Class totals must be non-negative.")
                    return
                }
                calculator.calculateMeanAndStdDev(parsed)
            }

            GradeCalcMode.DIRECT_MU_SIGMA -> {
                val mu = s.meanInput.toDoubleOrNull()
                val sd = s.sigmaInput.toDoubleOrNull()
                if (mu == null || sd == null) {
                    showError("Enter valid values for class average (mu) and standard deviation (sigma).")
                    return
                }
                if (mu < 0.0 || sd < 0.0) {
                    showError("Class average and sigma cannot be negative.")
                    return
                }
                mu to sd
            }
        }

        if (tee == null || attendance == null || otherAssessment == null) {
            showError("Enter valid TEE, attendance and other assessment marks.")
            return
        }

        if (tee !in 0.0..100.0 || attendance < 0.0 || otherAssessment < 0.0) {
            showError("TEE must be 0-100. Attendance and other assessment marks cannot be negative.")
            return
        }

        if (s.useMidTermFormula) {
            if (midTerm == null || midTerm !in 0.0..100.0) {
                showError("Mid Term marks must be between 0 and 100.")
                return
            }
        } else {
            if (cat1 == null || cat2 == null || cat1 !in 0.0..50.0 || cat2 !in 0.0..50.0) {
                showError("CAT-1 and CAT-2 marks must be between 0 and 50.")
                return
            }
        }

        val result = calculator.calculateGrade(
            RelativeGradeCalculator.GradeInput(
                formula = if (s.useMidTermFormula) {
                    RelativeGradeCalculator.TotalFormula.MIDTERM_TEE
                } else {
                    RelativeGradeCalculator.TotalFormula.CAT1_CAT2_TEE
                },
                cat1 = cat1,
                cat2 = cat2,
                midTerm = midTerm,
                tee = tee,
                attendance = attendance,
                otherAssessment = otherAssessment,
                mean = mean,
                standardDeviation = sigma
            )
        )

        when (result) {
            is RelativeGradeCalculator.Validation.Error -> showError(result.message)
            is RelativeGradeCalculator.Validation.Success -> {
                val r = result.result
                _uiState.value = s.copy(
                    resultTitle = "Grade: ${r.letterGrade}  |  Grade Point: ${r.gradePoint}",
                    resultSubtitle = "Total: ${fmt(r.totalMarks)}   mu: ${fmt(r.mean)}   sigma: ${fmt(r.standardDeviation)}",
                    isError = false
                )
            }
        }
    }

    fun reset() {
        _uiState.value = GradeCalculatorUiState(mode = _uiState.value.mode)
    }

    private fun parseClassTotals(raw: String): List<Double> {
        return raw
            .split(",", " ", "\n", "\t")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { it.toDoubleOrNull() }
    }

    private fun fmt(value: Double): String = String.format(Locale.US, "%.2f", value)

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            resultTitle = message,
            resultSubtitle = null,
            isError = true
        )
    }
}
