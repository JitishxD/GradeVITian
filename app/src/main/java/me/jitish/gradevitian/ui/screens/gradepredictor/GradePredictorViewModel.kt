package me.jitish.gradevitian.ui.screens.gradepredictor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.jitish.gradevitian.domain.calculator.GradePredictor
import javax.inject.Inject

data class GradePredictorUiState(
    val courseCredits: String = "",
    val theoryCredits: String = "",
    val labCredits: String = "",
    val jCompCredits: String = "",
    // Theory
    val cat1: String = "",
    val cat2: String = "",
    val da1: String = "",
    val da2: String = "",
    val da3: String = "",
    val theoryFat: String = "",
    val additionalLearning: String = "",
    // Lab
    val labInternal: String = "",
    val labFat: String = "",
    // J-Component
    val review1: String = "",
    val review2: String = "",
    val review3: String = "",
    // Weightage converter
    val maxOriginal: String = "",
    val maxWeightage: String = "",
    val obtainedOriginal: String = "",
    val weightageResult: String? = null,
    // Result
    val resultTitle: String? = null,
    val resultSubtitle: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class GradePredictorViewModel @Inject constructor(
    private val gradePredictor: GradePredictor
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradePredictorUiState())
    val uiState: StateFlow<GradePredictorUiState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.value = when (field) {
            "courseCredits" -> _uiState.value.copy(courseCredits = value, resultTitle = null)
            "theoryCredits" -> _uiState.value.copy(theoryCredits = value, resultTitle = null)
            "labCredits" -> _uiState.value.copy(labCredits = value, resultTitle = null)
            "jCompCredits" -> _uiState.value.copy(jCompCredits = value, resultTitle = null)
            "cat1" -> _uiState.value.copy(cat1 = value, resultTitle = null)
            "cat2" -> _uiState.value.copy(cat2 = value, resultTitle = null)
            "da1" -> _uiState.value.copy(da1 = value, resultTitle = null)
            "da2" -> _uiState.value.copy(da2 = value, resultTitle = null)
            "da3" -> _uiState.value.copy(da3 = value, resultTitle = null)
            "theoryFat" -> _uiState.value.copy(theoryFat = value, resultTitle = null)
            "additionalLearning" -> _uiState.value.copy(additionalLearning = value, resultTitle = null)
            "labInternal" -> _uiState.value.copy(labInternal = value, resultTitle = null)
            "labFat" -> _uiState.value.copy(labFat = value, resultTitle = null)
            "review1" -> _uiState.value.copy(review1 = value, resultTitle = null)
            "review2" -> _uiState.value.copy(review2 = value, resultTitle = null)
            "review3" -> _uiState.value.copy(review3 = value, resultTitle = null)
            "maxOriginal" -> _uiState.value.copy(maxOriginal = value, weightageResult = null)
            "maxWeightage" -> _uiState.value.copy(maxWeightage = value, weightageResult = null)
            "obtainedOriginal" -> _uiState.value.copy(obtainedOriginal = value, weightageResult = null)
            else -> _uiState.value
        }
    }

    fun predict() {
        val s = _uiState.value
        val input = GradePredictor.PredictionInput(
            courseCredits = s.courseCredits.toIntOrNull() ?: 0,
            theoryCredits = s.theoryCredits.toIntOrNull() ?: 0,
            labCredits = s.labCredits.toIntOrNull() ?: 0,
            jCompCredits = s.jCompCredits.toIntOrNull() ?: 0,
            cat1 = s.cat1.toDoubleOrNull(),
            cat2 = s.cat2.toDoubleOrNull(),
            da1 = s.da1.toDoubleOrNull(),
            da2 = s.da2.toDoubleOrNull(),
            da3 = s.da3.toDoubleOrNull(),
            theoryFat = s.theoryFat.toDoubleOrNull(),
            additionalLearning = s.additionalLearning.toDoubleOrNull(),
            labInternal = s.labInternal.toDoubleOrNull(),
            labFat = s.labFat.toDoubleOrNull(),
            review1 = s.review1.toDoubleOrNull(),
            review2 = s.review2.toDoubleOrNull(),
            review3 = s.review3.toDoubleOrNull()
        )
        when (val result = gradePredictor.predict(input)) {
            is GradePredictor.PredictionValidation.Success -> {
                _uiState.value = s.copy(
                    resultTitle = "Total Marks: ${result.result.totalMarks} ~ ${result.result.roundedMarks}",
                    resultSubtitle = result.result.message,
                    isError = false
                )
            }
            is GradePredictor.PredictionValidation.Error -> {
                _uiState.value = s.copy(
                    resultTitle = result.message,
                    resultSubtitle = result.detail,
                    isError = true
                )
            }
        }
    }

    fun convertWeightage() {
        val s = _uiState.value
        val result = gradePredictor.convertWeightage(
            maxOriginal = s.maxOriginal.toDoubleOrNull() ?: 0.0,
            maxWeightage = s.maxWeightage.toDoubleOrNull() ?: 0.0,
            obtainedOriginal = s.obtainedOriginal.toDoubleOrNull() ?: 0.0
        )
        _uiState.value = s.copy(weightageResult = result)
    }

    fun reset() {
        _uiState.value = GradePredictorUiState()
    }

    fun resetWeightage() {
        _uiState.value = _uiState.value.copy(
            maxOriginal = "", maxWeightage = "", obtainedOriginal = "", weightageResult = null
        )
    }
}

