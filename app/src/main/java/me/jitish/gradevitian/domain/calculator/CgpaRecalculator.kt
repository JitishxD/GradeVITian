package me.jitish.gradevitian.domain.calculator

import me.jitish.gradevitian.domain.model.Grade
import java.util.Locale
import javax.inject.Inject

/**
 * CGPA Recalculator — impact of improving one course grade on overall CGPA.
 *
 * Formula: newCGPA = (currentCGPA × totalCredits + (newGradePoint − oldGradePoint) × courseCredits) / totalCredits
 *
 * Only the changed course matters; semester GPA is not required.
 */
class CgpaRecalculator @Inject constructor() {

    data class RecalculationResult(
        val oldCgpa: Double,
        val newCgpa: Double,
        val delta: Double,
        val message: String
    )

    sealed class RecalculationValidation {
        data class Success(val result: RecalculationResult) : RecalculationValidation()
        data class Error(val message: String, val detail: String = "") : RecalculationValidation()
    }

    fun recalculateFromGradeChange(
        currentCgpa: Double,
        totalCredits: Int,
        courseCredits: Int,
        oldGrade: Grade,
        newGrade: Grade
    ): RecalculationValidation {
        if (currentCgpa <= 0 || totalCredits <= 0 || courseCredits <= 0) {
            return RecalculationValidation.Error(
                "Kindly check your entries.",
                "It shouldn't be zero, negative, special, text or empty."
            )
        }
        if (currentCgpa > 10) {
            return RecalculationValidation.Error(
                "Kindly check your entries.",
                "Average limit (0 < CGPA <= 10)."
            )
        }
        if (totalCredits > 300 || courseCredits > 50) {
            return RecalculationValidation.Error(
                "Kindly check your entries.",
                "Credits limitation (1 <= Total Credits <= 300 & 1 <= Course Credits <= 50). Exclude P/pass-fail credits."
            )
        }
        if (courseCredits > totalCredits) {
            return RecalculationValidation.Error(
                "Course credits cannot exceed total credits.",
                "Total credits should include this course."
            )
        }

        val oldPoint = oldGrade.gradePoint
        val newPoint = newGrade.gradePoint
        if (!oldGrade.countsTowardGpa || oldPoint == null || oldGrade == Grade.NONE) {
            return RecalculationValidation.Error(
                "Select a valid old grade.",
                "P/pass-fail and empty grades are excluded from CGPA."
            )
        }
        if (!newGrade.countsTowardGpa || newPoint == null || newGrade == Grade.NONE) {
            return RecalculationValidation.Error(
                "Select a valid new grade.",
                "P/pass-fail and empty grades are excluded from CGPA."
            )
        }

        val weightedSum = currentCgpa * totalCredits + (newPoint - oldPoint) * courseCredits
        val newCgpa = weightedSum / totalCredits

        if (newCgpa <= 0) {
            return RecalculationValidation.Error("Oops! Your entries are incorrect.")
        }
        if (newCgpa > 10) {
            return RecalculationValidation.Error(
                "Invalid input. Kindly check your entries.",
                "Recalculated CGPA cannot exceed 10."
            )
        }

        val oldRounded = String.format(Locale.US, "%.4f", currentCgpa).toDouble()
        val newRounded = String.format(Locale.US, "%.4f", newCgpa).toDouble()
        val delta = String.format(Locale.US, "%.4f", newRounded - oldRounded).toDouble()

        val message = when {
            delta > 0 && newRounded >= 9.0 -> "Nice improvement! Keep it up and Happy Learning!"
            delta > 0 -> "Your CGPA improved. Happy Learning!"
            delta < 0 -> "Your CGPA decreased. Double-check your entries."
            else -> "No change — old and new grades are the same."
        }

        return RecalculationValidation.Success(
            RecalculationResult(
                oldCgpa = oldRounded,
                newCgpa = newRounded,
                delta = delta,
                message = message
            )
        )
    }
}
