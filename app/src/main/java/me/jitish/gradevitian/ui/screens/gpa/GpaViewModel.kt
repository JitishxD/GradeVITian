package me.jitish.gradevitian.ui.screens.gpa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.jitish.gradevitian.domain.calculator.GpaCalculator
import me.jitish.gradevitian.domain.model.CourseEntry
import me.jitish.gradevitian.domain.model.GpaRecord
import me.jitish.gradevitian.domain.model.Grade
import me.jitish.gradevitian.domain.repository.AuthRepository
import me.jitish.gradevitian.domain.repository.RecordsRepository
import me.jitish.gradevitian.ui.navigation.PendingRecordHolder
import javax.inject.Inject

data class GpaUiState(
    val courses: List<CourseEntry> = listOf(CourseEntry(id = 1)),
    val resultTitle: String? = null,
    val resultSubtitle: String? = null,
    val isError: Boolean = false,
    val isSaved: Boolean = false,
    val saveMessage: String? = null
)

@HiltViewModel
class GpaViewModel @Inject constructor(
    private val gpaCalculator: GpaCalculator,
    private val authRepository: AuthRepository,
    private val recordsRepository: RecordsRepository,
    private val pendingRecordHolder: PendingRecordHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(GpaUiState())
    val uiState: StateFlow<GpaUiState> = _uiState.asStateFlow()

    init {
        pendingRecordHolder.pendingGpaRecord?.let { record ->
            pendingRecordHolder.pendingGpaRecord = null
            loadFromRecord(record)
        }
    }

    fun updateCredit(courseId: Int, credits: Int) {
        val courses = _uiState.value.courses.map {
            if (it.id == courseId) it.copy(credits = credits) else it
        }
        _uiState.value = _uiState.value.copy(courses = courses, resultTitle = null, isSaved = false)
    }

    fun updateGrade(courseId: Int, grade: Grade) {
        val courses = _uiState.value.courses.map {
            if (it.id == courseId) it.copy(grade = grade) else it
        }
        _uiState.value = _uiState.value.copy(courses = courses, resultTitle = null, isSaved = false)
    }

    fun addCourse() {
        val courses = _uiState.value.courses
        val newId = (courses.maxOfOrNull { it.id } ?: 0) + 1
        _uiState.value = _uiState.value.copy(courses = courses + CourseEntry(id = newId))
    }

    fun removeCourse(courseId: Int) {
        if (_uiState.value.courses.size <= 1) return
        val courses = _uiState.value.courses.filter { it.id != courseId }
        _uiState.value = _uiState.value.copy(courses = courses)
    }

    fun calculate() {
        when (val result = gpaCalculator.calculate(_uiState.value.courses)) {
            is GpaCalculator.GpaValidation.Success -> {
                _uiState.value = _uiState.value.copy(
                    resultTitle = "Your GPA is ${result.result.gpa}",
                    resultSubtitle = result.result.message,
                    isError = false
                )
            }
            is GpaCalculator.GpaValidation.Error -> {
                _uiState.value = _uiState.value.copy(
                    resultTitle = result.message,
                    resultSubtitle = result.detail,
                    isError = true
                )
            }
        }
    }

    fun reset() {
        _uiState.value = GpaUiState()
    }

    fun loadFromRecord(record: GpaRecord) {
        val courses = if (record.courses.isNotEmpty()) {
            record.courses.mapIndexed { index, course -> course.copy(id = index + 1) }
        } else {
            listOf(CourseEntry(id = 1))
        }
        _uiState.value = GpaUiState(courses = courses)
        calculate()
    }

    fun saveRecord() {
        val userId = authRepository.currentUserId
        if (userId == null) {
            _uiState.value = _uiState.value.copy(saveMessage = "Sign in to save records")
            return
        }
        val result = gpaCalculator.calculate(_uiState.value.courses)
        if (result !is GpaCalculator.GpaValidation.Success) {
            _uiState.value = _uiState.value.copy(saveMessage = "Calculate first before saving")
            return
        }

        viewModelScope.launch {
            val record = GpaRecord(
                courses = _uiState.value.courses.filter { it.credits > 0 && it.grade != Grade.NONE },
                gpa = result.result.gpa,
                totalCredits = result.result.totalCredits
            )
            val saveResult = recordsRepository.saveGpaRecord(userId, record)
            _uiState.value = _uiState.value.copy(
                isSaved = saveResult is me.jitish.gradevitian.domain.util.Resource.Success,
                saveMessage = if (saveResult is me.jitish.gradevitian.domain.util.Resource.Success) "Saved!" else "Failed to save"
            )
        }
    }

    fun clearSaveMessage() {
        _uiState.value = _uiState.value.copy(saveMessage = null)
    }
}

