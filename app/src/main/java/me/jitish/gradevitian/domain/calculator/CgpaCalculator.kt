package me.jitish.gradevitian.domain.calculator

import me.jitish.gradevitian.domain.model.SemesterEntry
import javax.inject.Inject

/**
 * CGPA Calculator - matches referenceWebCode/CGPA Calculator.js exactly.
 *
 * Semester-wise: CGPA = Σ(semCredits × semGPA) / Σ(semCredits)
 * Result rounded to 4 decimal places.
 */
class CgpaCalculator @Inject constructor() {

    data class CgpaResult(
        val cgpa: Double,
        val totalCredits: Int,
        val message: String
    )

    sealed class CgpaValidation {
        data class Success(val result: CgpaResult) : CgpaValidation()
        data class Error(val message: String, val detail: String = "") : CgpaValidation()
    }

    fun calculate(semesters: List<SemesterEntry>): CgpaValidation {
        val active = semesters.filter { it.credits > 0 || it.gpa > 0.0 }

        // Validate negative GPA
        if (active.any { it.gpa < 0.0 }) {
            return CgpaValidation.Error(
                "Kindly check your respective GPA entries.",
                "It shouldn't be negative, special or text."
            )
        }

        // Validate limits
        if (active.any { it.gpa > 10.0 || it.credits > 50 }) {
            return CgpaValidation.Error(
                "Kindly check the CREDITS and GPA limits.",
                "(0 < CREDITS <= 50) & (0 < GPA <= 10)"
            )
        }

        // Credits > 0 but GPA == 0
        if (active.any { it.credits > 0 && it.gpa == 0.0 }) {
            return CgpaValidation.Error(
                "GPA entries in the respective semesters aren't entered where your credits are greater than zero."
            )
        }

        // GPA > 0 but credits == 0
        if (active.any { it.credits == 0 && it.gpa > 0.0 }) {
            return CgpaValidation.Error(
                "Credit entries in the respective semesters aren't entered where your GPA is greater than zero."
            )
        }

        val valid = active.filter { it.credits > 0 && it.gpa > 0.0 }

        if (valid.isEmpty()) {
            return CgpaValidation.Error("Kindly enter your semester-wise credits and GPA.")
        }

        val totalCredits = valid.sumOf { it.credits }
        val weightedSum = valid.sumOf { it.credits * it.gpa }
        val cgpa = if (totalCredits > 0) weightedSum / totalCredits else 0.0

        val message = when {
            cgpa >= 9.0 -> "You are awesome! Keep it up and Happy Learning!"
            cgpa >= 8.0 -> "You are at it. Excel in upcoming semesters. I'm very eager to see you as 9 pointer."
            else -> "Happy Learning!"
        }

        return CgpaValidation.Success(
            CgpaResult(
                cgpa = String.format("%.4f", cgpa).toDouble(),
                totalCredits = totalCredits,
                message = message
            )
        )
    }

    /**
     * Instant CGPA Calculator - matches icgpacalculation() in CGPA Calculator.js
     * Formula: newCGPA = (desiredGPA × newCredits + currentCGPA × completedCredits) / (newCredits + completedCredits)
     */
    fun calculateInstantCgpa(
        currentCgpa: Double,
        completedCredits: Int,
        currentSemGpa: Double,
        currentSemCredits: Int
    ): CgpaValidation {
        if (currentCgpa <= 0 || completedCredits <= 0 || currentSemGpa <= 0 || currentSemCredits <= 0) {
            return CgpaValidation.Error(
                "Kindly check your entries.",
                "It shouldn't be zero, negative, special, text or empty."
            )
        }
        if (currentCgpa > 10 || currentSemGpa > 10) {
            return CgpaValidation.Error(
                "Kindly check your entries.",
                "Average limit (0 < GPA or CGPA <= 10)."
            )
        }
        if (completedCredits > 300 || currentSemCredits > 50) {
            return CgpaValidation.Error(
                "Kindly check your entries.",
                "Credits limitation (1 <= Credits Completed <= 300 & 1 <= Credits this sem <= 50)."
            )
        }

        val result = (currentSemGpa * currentSemCredits + currentCgpa * completedCredits) /
                (currentSemCredits + completedCredits)

        if (result <= 0) {
            return CgpaValidation.Error("Oops! Your entries are incorrect.")
        }
        if (result > 10) {
            return CgpaValidation.Error("Invalid input. Kindly check your entries.", "You can proceed again.")
        }

        val message = if (result >= 9.0) "You are Terrific! and Happy Learning!" else "Happy Learning!"

        return CgpaValidation.Success(
            CgpaResult(
                cgpa = String.format("%.3f", result).toDouble(),
                totalCredits = completedCredits + currentSemCredits,
                message = message
            )
        )
    }
}

