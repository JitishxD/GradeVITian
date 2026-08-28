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

    /**
     * GPA Recalculator — impact of improving one course grade on semester GPA.
     *
     * Formula: newGPA = (currentGPA × semTotalCredits + (newGradePoint − oldGradePoint) × courseCredits) / semTotalCredits
     */
    fun recalculateGpaFromGradeChange(
        currentGpa: Double,
        semTotalCredits: Int,
        courseCredits: Int,
        oldGrade: Grade,
        newGrade: Grade
    ): RecalculationValidation {
        if (currentGpa <= 0 || semTotalCredits <= 0 || courseCredits <= 0) {
            return RecalculationValidation.Error(
                "Kindly check your entries.",
                "It shouldn't be zero, negative, special, text or empty."
            )
        }
        if (currentGpa > 10) {
            return RecalculationValidation.Error(
                "Kindly check your entries.",
                "Average limit (0 < GPA <= 10)."
            )
        }
        if (semTotalCredits > 50 || courseCredits > 50) {
            return RecalculationValidation.Error(
                "Kindly check your entries.",
                "Credits limitation (1 <= Semester Credits <= 50 & 1 <= Course Credits <= 50). Exclude P/pass-fail credits."
            )
        }
        if (courseCredits > semTotalCredits) {
            return RecalculationValidation.Error(
                "Course credits cannot exceed semester credits.",
                "Semester credits should include this course."
            )
        }

        val oldPoint = oldGrade.gradePoint
        val newPoint = newGrade.gradePoint
        if (!oldGrade.countsTowardGpa || oldPoint == null || oldGrade == Grade.NONE) {
            return RecalculationValidation.Error(
                "Select a valid old grade.",
                "P/pass-fail and empty grades are excluded from GPA."
            )
        }
        if (!newGrade.countsTowardGpa || newPoint == null || newGrade == Grade.NONE) {
            return RecalculationValidation.Error(
                "Select a valid new grade.",
                "P/pass-fail and empty grades are excluded from GPA."
            )
        }

        val weightedSum = currentGpa * semTotalCredits + (newPoint - oldPoint) * courseCredits
        val newGpa = weightedSum / semTotalCredits

        if (newGpa <= 0) {
            return RecalculationValidation.Error("Oops! Your entries are incorrect.")
        }
        if (newGpa > 10) {
            return RecalculationValidation.Error(
                "Invalid input. Kindly check your entries.",
                "Recalculated GPA cannot exceed 10."
            )
        }

        val oldRounded = String.format(Locale.US, "%.4f", currentGpa).toDouble()
        val newRounded = String.format(Locale.US, "%.4f", newGpa).toDouble()
        val delta = String.format(Locale.US, "%.4f", newRounded - oldRounded).toDouble()

        val message = when {
            delta > 0 && newRounded >= 9.0 -> "Nice improvement! Keep it up and Happy Learning!"
            delta > 0 -> "Your GPA improved. Happy Learning!"
            delta < 0 -> "Your GPA decreased. Double-check your entries."
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

    /**
     * CGPA Recalculator from GPA change — impact of improved semester GPA on overall CGPA.
     *
     * Formula: newCGPA = (oldCGPA × totalCredits + (newGPA − oldGPA) × semCredits) / totalCredits
     */
    fun recalculateCgpaFromGpaChange(
        oldCgpa: Double,
        totalCredits: Int,
        semCredits: Int,
        oldGpa: Double,
        newGpa: Double
    ): RecalculationValidation {
        if (oldCgpa <= 0 || totalCredits <= 0 || semCredits <= 0) {
            return RecalculationValidation.Error(
                "Kindly check your entries.",
                "It shouldn't be zero, negative, special, text or empty."
            )
        }
        if (oldCgpa > 10 || oldGpa <= 0 || oldGpa > 10 || newGpa <= 0 || newGpa > 10) {
            return RecalculationValidation.Error(
                "Kindly check your entries.",
                "Average limit (0 < CGPA/GPA <= 10)."
            )
        }
        if (totalCredits > 300 || semCredits > 50) {
            return RecalculationValidation.Error(
                "Kindly check your entries.",
                "Credits limitation (1 <= Total Credits <= 300 & 1 <= Semester Credits <= 50). Exclude P/pass-fail credits."
            )
        }
        if (semCredits > totalCredits) {
            return RecalculationValidation.Error(
                "Semester credits cannot exceed total credits.",
                "Total credits should include this semester."
            )
        }

        val weightedSum = oldCgpa * totalCredits + (newGpa - oldGpa) * semCredits
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

        val oldRounded = String.format(Locale.US, "%.4f", oldCgpa).toDouble()
        val newRounded = String.format(Locale.US, "%.4f", newCgpa).toDouble()
        val delta = String.format(Locale.US, "%.4f", newRounded - oldRounded).toDouble()

        val message = when {
            delta > 0 && newRounded >= 9.0 -> "Nice improvement! Keep it up and Happy Learning!"
            delta > 0 -> "Your CGPA improved. Happy Learning!"
            delta < 0 -> "Your CGPA decreased. Double-check your entries."
            else -> "No change — old and new GPAs are the same."
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
