package me.jitish.gradevitian.domain.calculator

import me.jitish.gradevitian.domain.model.CourseEntry
import me.jitish.gradevitian.domain.model.Grade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GpaCalculatorTest {

    private val calculator = GpaCalculator()

    @Test
    fun `p grade courses are excluded from GPA numerator and denominator`() {
        val result = calculator.calculate(
            listOf(
                CourseEntry(id = 1, credits = 4, grade = Grade.A),
                CourseEntry(id = 2, credits = 3, grade = Grade.B),
                CourseEntry(id = 3, credits = 2, grade = Grade.P)
            )
        )

        assertTrue(result is GpaCalculator.GpaValidation.Success)
        val gpaResult = (result as GpaCalculator.GpaValidation.Success).result
        assertEquals(8.5714, gpaResult.gpa, 0.00001)
        assertEquals(7, gpaResult.totalCredits)
        assertEquals(60, gpaResult.totalGradePoints)
    }

    @Test
    fun `p grade has no grade point and does not count toward GPA`() {
        assertEquals(Grade.P, Grade.fromLabel("p"))
        assertNull(Grade.P.gradePoint)
        assertFalse(Grade.P.countsTowardGpa)
    }

    @Test
    fun `only p grade courses cannot produce a GPA`() {
        val result = calculator.calculate(
            listOf(
                CourseEntry(id = 1, credits = 2, grade = Grade.P)
            )
        )

        assertTrue(result is GpaCalculator.GpaValidation.Error)
        assertTrue((result as GpaCalculator.GpaValidation.Error).message.contains("graded course"))
    }
}
