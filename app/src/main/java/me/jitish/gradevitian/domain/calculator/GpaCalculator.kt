package me.jitish.gradevitian.domain.calculator

import me.jitish.gradevitian.domain.model.CourseEntry
import me.jitish.gradevitian.domain.model.Grade
import java.util.Locale
import javax.inject.Inject

/**
 * GPA Calculator.
 *
 * Formula: GPA = Σ(grade_point × credits) / Σ(credits)
 * Grade points: S=10, A=9, B=8, C=7, D=6, E=5, F=0, N=0
 * P is pass/fail and excluded from both sums.
 * Result rounded to 4 decimal places.
 */
class GpaCalculator @Inject constructor() {

    data class GpaResult(
        val gpa: Double,
        val totalCredits: Int,
        val totalGradePoints: Int,
        val message: String
    )

    sealed class GpaValidation {
        data class Success(val result: GpaResult) : GpaValidation()
        data class Error(val message: String, val detail: String = "") : GpaValidation()
    }

    fun calculate(courses: List<CourseEntry>): GpaValidation {
        // Filter out empty entries
        val activeCourses = courses.filter { it.credits > 0 || it.grade != Grade.NONE }

        // Validate: grade selected but no credit
        val gradeWithoutCredit = activeCourses.any { it.grade != Grade.NONE && it.credits == 0 }
        if (gradeWithoutCredit) {
            return GpaValidation.Error(
                "Please select the respective credit(s) for the course(s) of which grade(s) are already opted."
            )
        }

        // Validate: credit entered but no grade
        val creditWithoutGrade = activeCourses.any { it.credits > 0 && it.grade == Grade.NONE }
        if (creditWithoutGrade) {
            return GpaValidation.Error(
                "Please select the respective grade(s) for the course(s) of which credit(s) are already opted."
            )
        }

        val completedCourses = activeCourses.filter { it.credits > 0 && it.grade != Grade.NONE }

        if (completedCourses.isEmpty()) {
            return GpaValidation.Error(
                "Please enter at least one course with credits and grade."
            )
        }

        val gpaCourses = completedCourses.filter { it.grade.countsTowardGpa }

        if (gpaCourses.isEmpty()) {
            return GpaValidation.Error(
                "Please enter at least one graded course with credits.",
                "P/pass-fail courses are completed credits, but they are excluded from GPA."
            )
        }

        val totalCredits = gpaCourses.sumOf { it.credits }
        val totalGradePoints = gpaCourses.sumOf { (it.grade.gradePoint ?: 0) * it.credits }

        if (totalCredits == 0) {
            return GpaValidation.Error("Total credits cannot be zero.")
        }

        val gpa = totalGradePoints.toDouble() / totalCredits.toDouble()

        val message = when {
            gpa >= 9.0 -> "You are awesome! Keep it up and Happy Learning!"
            gpa >= 8.0 -> "You are at it. Excel in upcoming semesters. I'm very eager to see you as 9 pointer."
            gpa == 0.0 -> "I didn't get any input from you (or) You got all N/F grades."
            else -> "Happy Learning!"
        }

        return GpaValidation.Success(
            GpaResult(
                gpa = String.format(Locale.US, "%.4f", gpa).toDouble(),
                totalCredits = totalCredits,
                totalGradePoints = totalGradePoints,
                message = message
            )
        )
    }
}
