package me.jitish.gradevitian.domain.calculator

import me.jitish.gradevitian.domain.model.SemesterEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CgpaCalculatorTest {

    private val calculator = CgpaCalculator()

    @Test
    fun `semester wise CGPA uses the provided GPA counted credits as denominator`() {
        val result = calculator.calculate(
            listOf(
                SemesterEntry(semesterNumber = 1, credits = 7, gpa = 8.5714),
                SemesterEntry(semesterNumber = 2, credits = 4, gpa = 9.0)
            )
        )

        assertTrue(result is CgpaCalculator.CgpaValidation.Success)
        val cgpaResult = (result as CgpaCalculator.CgpaValidation.Success).result
        assertEquals(8.7273, cgpaResult.cgpa, 0.00001)
        assertEquals(11, cgpaResult.totalCredits)
    }
}
