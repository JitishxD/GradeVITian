package me.jitish.gradevitian.domain.calculator

import javax.inject.Inject

/**
 * CGPA Estimator - matches referenceWebCode/CGPA Estimator.js exactly.
 *
 * Formula: requiredGPA = (desiredCGPA × (completedCredits + newCredits) - currentCGPA × completedCredits) / newCredits
 */
class CgpaEstimator @Inject constructor() {

    data class EstimationResult(
        val requiredGpa: Double,
        val message: String
    )

    sealed class EstimationValidation {
        data class Success(val result: EstimationResult) : EstimationValidation()
        data class Error(val message: String, val detail: String = "") : EstimationValidation()
    }

    fun estimate(
        desiredCgpa: Double,
        currentCgpa: Double,
        completedCredits: Int,
        newCredits: Int
    ): EstimationValidation {
        if (desiredCgpa <= 0 || currentCgpa <= 0 || completedCredits <= 0 || newCredits <= 0) {
            return EstimationValidation.Error(
                "Kindly check your entries.",
                "It shouldn't be zero or empty."
            )
        }
        if (desiredCgpa > 10 || currentCgpa > 10) {
            return EstimationValidation.Error(
                "Kindly check your entries.",
                "Average limit of GPA or CGPA is (0 < X <= 10)."
            )
        }
        if (completedCredits > 300 || newCredits > 50) {
            return EstimationValidation.Error(
                "Kindly check your entries.",
                "Credits limitation (1 <= Credits Completed <= 300 & 1 <= Credits Taken <= 50)."
            )
        }

        val requiredGpa = (desiredCgpa * (completedCredits + newCredits) - currentCgpa * completedCredits) / newCredits

        if (requiredGpa <= 0) {
            return EstimationValidation.Error(
                "Oops! Your entries are incorrect (or) You need not attend your Final Examination."
            )
        }
        if (requiredGpa > 10) {
            return EstimationValidation.Error(
                "$newCredits Credit(s) you have taken aren't enough to get $desiredCgpa CGPA. Excel in the upcoming semesters.",
                "Just missed. You are at it. Best of luck next time!"
            )
        }

        val message = if (requiredGpa >= 9.0) {
            "You are Terrific! and Happy Learning!"
        } else {
            "Happy Learning!"
        }

        return EstimationValidation.Success(
            EstimationResult(
                requiredGpa = String.format("%.3f", requiredGpa).toDouble(),
                message = message
            )
        )
    }
}

