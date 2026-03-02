package me.jitish.gradevitian.domain.calculator

import javax.inject.Inject

/**
 * Attendance Calculator - matches referenceWebCode/Attendance Calculator.js exactly.
 *
 * Format 1 (Simple): attendance% = classesPresent / (classesPresent + classesAbsent) × 100
 * Format 2 (Total):  attendance% = classesPresent / totalClasses × 100
 *                  OR attendance% = (totalClasses - classesAbsent) / totalClasses × 100
 */
class AttendanceCalculator @Inject constructor() {

    data class AttendanceResult(
        val percentage: Double,
        val status: AttendanceStatus,
        val message: String
    )

    enum class AttendanceStatus { GOOD, WARNING, DANGER }

    sealed class AttendanceValidation {
        data class Success(val result: AttendanceResult) : AttendanceValidation()
        data class Error(val message: String) : AttendanceValidation()
    }

    /**
     * Format 1 - cdivideBy() in JS: classes present + classes absent
     */
    fun calculateSimple(classesPresent: Int, classesAbsent: Int): AttendanceValidation {
        if (classesPresent < 0 || classesAbsent < 0) {
            return AttendanceValidation.Error("Kindly check your entries, shouldn't be negative.")
        }
        if (classesPresent == 0 && classesAbsent == 0) {
            return AttendanceValidation.Success(
                AttendanceResult(0.0, AttendanceStatus.DANGER, "Your attendance is 0%")
            )
        }

        val total = classesPresent + classesAbsent
        val percentage = (classesPresent.toDouble() / total) * 100.0

        if (percentage > 100) {
            return AttendanceValidation.Error("Kindly check your entries.")
        }

        return buildResult(percentage)
    }

    /**
     * Format 2 - divideBy() in JS: total classes + present OR absent (not both)
     */
    fun calculateDetailed(
        totalClasses: Int,
        classesPresent: Int?,
        classesAbsent: Int?
    ): AttendanceValidation {
        val present = classesPresent ?: 0
        val absent = classesAbsent ?: 0

        if (totalClasses < 1 || present < 0 || absent < 0) {
            return AttendanceValidation.Error("Kindly check your entries, shouldn't be negative or empty.")
        }

        if (present > 0 && absent > 0) {
            return AttendanceValidation.Error(
                "Kindly enter either no. of classes present (or) no. of classes absent, but not both."
            )
        }

        if (present == 0 && absent == 0) {
            return AttendanceValidation.Success(
                AttendanceResult(0.0, AttendanceStatus.DANGER, "Your attendance is 0%")
            )
        }

        val percentage = if (absent > 0) {
            ((totalClasses - absent).toDouble() / totalClasses) * 100.0
        } else {
            (present.toDouble() / totalClasses) * 100.0
        }

        if (percentage > 100 || percentage < 0) {
            return AttendanceValidation.Error("Kindly check your entries.")
        }

        return buildResult(percentage)
    }

    private fun buildResult(percentage: Double): AttendanceValidation.Success {
        val formatted = String.format("%.2f", percentage).toDouble()
        val status = when {
            formatted >= 75.0 -> AttendanceStatus.GOOD
            formatted >= 50.0 -> AttendanceStatus.WARNING
            else -> AttendanceStatus.DANGER
        }
        return AttendanceValidation.Success(
            AttendanceResult(
                percentage = formatted,
                status = status,
                message = "Your attendance is ${formatted}%"
            )
        )
    }
}

