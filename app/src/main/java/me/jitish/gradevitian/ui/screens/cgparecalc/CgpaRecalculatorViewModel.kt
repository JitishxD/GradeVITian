package me.jitish.gradevitian.ui.screens.cgparecalc

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.jitish.gradevitian.domain.calculator.CgpaRecalculator
import me.jitish.gradevitian.domain.model.Grade
import java.util.Locale
import javax.inject.Inject

val GRADE_OPTIONS = listOf("S", "A", "B", "C", "D", "E", "F", "N")

data class CgpaRecalculatorUiState(
    // CGPA Recalculator tab
    val currentCgpa: String = "",
    val totalCredits: String = "",
    val courseCredits: String = "",
    val oldGrade: String = "-",
    val newGrade: String = "-",
    val resultTitle: String? = null,
    val resultSubtitle: String? = null,
    val isError: Boolean = false,
    // GPA Recalculator tab
    val currentGpa: String = "",
    val semTotalCredits: String = "",
    val gpaCourseCredits: String = "",
    val gpaOldGrade: String = "-",
    val gpaNewGrade: String = "-",
    val gpaResultTitle: String? = null,
    val gpaResultSubtitle: String? = null,
    val gpaIsError: Boolean = false
)

@HiltViewModel
class CgpaRecalculatorViewModel @Inject constructor(
    private val cgpaRecalculator: CgpaRecalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(CgpaRecalculatorUiState())
    val uiState: StateFlow<CgpaRecalculatorUiState> = _uiState.asStateFlow()

    // ── CGPA tab ──

    fun updateField(field: String, value: String) {
        _uiState.value = when (field) {
            "currentCgpa" -> _uiState.value.copy(currentCgpa = value, resultTitle = null)
            "totalCredits" -> _uiState.value.copy(totalCredits = value, resultTitle = null)
            "courseCredits" -> _uiState.value.copy(courseCredits = value, resultTitle = null)
            "oldGrade" -> _uiState.value.copy(oldGrade = value, resultTitle = null)
            "newGrade" -> _uiState.value.copy(newGrade = value, resultTitle = null)
            else -> _uiState.value
        }
    }

    fun calculate() {
        val state = _uiState.value
        val result = cgpaRecalculator.recalculateFromGradeChange(
            currentCgpa = state.currentCgpa.toDoubleOrNull() ?: 0.0,
            totalCredits = state.totalCredits.toIntOrNull() ?: 0,
            courseCredits = state.courseCredits.toIntOrNull() ?: 0,
            oldGrade = Grade.fromLabel(state.oldGrade),
            newGrade = Grade.fromLabel(state.newGrade)
        )
        when (result) {
            is CgpaRecalculator.RecalculationValidation.Success -> {
                val deltaText = formatDelta(result.result.delta)
                _uiState.value = state.copy(
                    resultTitle = "${result.result.oldCgpa} → ${result.result.newCgpa} ($deltaText)",
                    resultSubtitle = result.result.message,
                    isError = false
                )
            }
            is CgpaRecalculator.RecalculationValidation.Error -> {
                _uiState.value = state.copy(
                    resultTitle = result.message,
                    resultSubtitle = result.detail,
                    isError = true
                )
            }
        }
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(
            currentCgpa = "", totalCredits = "", courseCredits = "",
            oldGrade = "-", newGrade = "-",
            resultTitle = null, resultSubtitle = null
        )
    }

    // ── GPA tab ──

    fun updateGpaField(field: String, value: String) {
        _uiState.value = when (field) {
            "currentGpa" -> _uiState.value.copy(currentGpa = value, gpaResultTitle = null)
            "semTotalCredits" -> _uiState.value.copy(semTotalCredits = value, gpaResultTitle = null)
            "gpaCourseCredits" -> _uiState.value.copy(gpaCourseCredits = value, gpaResultTitle = null)
            "gpaOldGrade" -> _uiState.value.copy(gpaOldGrade = value, gpaResultTitle = null)
            "gpaNewGrade" -> _uiState.value.copy(gpaNewGrade = value, gpaResultTitle = null)
            else -> _uiState.value
        }
    }

    fun calculateGpa() {
        val state = _uiState.value
        val result = cgpaRecalculator.recalculateGpaFromGradeChange(
            currentGpa = state.currentGpa.toDoubleOrNull() ?: 0.0,
            semTotalCredits = state.semTotalCredits.toIntOrNull() ?: 0,
            courseCredits = state.gpaCourseCredits.toIntOrNull() ?: 0,
            oldGrade = Grade.fromLabel(state.gpaOldGrade),
            newGrade = Grade.fromLabel(state.gpaNewGrade)
        )
        when (result) {
            is CgpaRecalculator.RecalculationValidation.Success -> {
                val deltaText = formatDelta(result.result.delta)
                _uiState.value = state.copy(
                    gpaResultTitle = "${result.result.oldCgpa} → ${result.result.newCgpa} ($deltaText)",
                    gpaResultSubtitle = result.result.message,
                    gpaIsError = false
                )
            }
            is CgpaRecalculator.RecalculationValidation.Error -> {
                _uiState.value = state.copy(
                    gpaResultTitle = result.message,
                    gpaResultSubtitle = result.detail,
                    gpaIsError = true
                )
            }
        }
    }

    fun resetGpa() {
        _uiState.value = _uiState.value.copy(
            currentGpa = "", semTotalCredits = "", gpaCourseCredits = "",
            gpaOldGrade = "-", gpaNewGrade = "-",
            gpaResultTitle = null, gpaResultSubtitle = null
        )
    }

    // ── Shared ──

    private fun formatDelta(delta: Double): String {
        val formatted = String.format(Locale.US, "%.4f", kotlin.math.abs(delta))
        return when {
            delta > 0 -> "+$formatted"
            delta < 0 -> "-$formatted"
            else -> "±0"
        }
    }
}
