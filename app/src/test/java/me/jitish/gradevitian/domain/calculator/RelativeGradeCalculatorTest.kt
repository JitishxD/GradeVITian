package me.jitish.gradevitian.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/*
| Field                                | Value            |
| ------------------------------------ | ---------------- |
| Course Code                          | MATXXXX          |
| Class No.                            | BL2023241100XXXX |
| μ (Class Average)                    | 77               |
| σ (Standard Deviation for the Class) | 7.5              |
| Reg. No.                             | 23BCGXXXXX       |
| Total Marks                          | 81               |
| (0.6Mid Term + 0.3TEE + Attendance + Other Assesment Marks)

| Relative Grading Formula                                           | Letter Grade | Grade Point |
| ------------------------------------------------------------------ | :----------: | :---------: |
| Total Marks ≥ 77 + 1.5 × 7.5 = 88.25                               |     S    |    10   |
| 77 + 0.5 × 7.5 = 80.75 ≤ Total Marks < 88.25                       |     A    |    9    |
| 77 − 0.5 × 7.5 = 73.25 ≤ Total Marks < 80.75                       |     B    |    8    |
| 77 − 7.5 = 69.5 ≤ Total Marks < 73.25                              |     C    |    7    |
| 77 − 1.5 × 7.5 = 65.75 ≤ Total Marks < 69.5                        |     D    |    6    |
| TEE ≥ 40 and (CAT1 + CAT2 + TEE ≥ 60), and Total Marks < 65.75     |     E    |    5    |
| TEE < 40 or CAT1 + CAT2 + TEE < 60                                 |     F    |    0    |
*/

class RelativeGradeCalculatorTest {

    private val calculator = RelativeGradeCalculator()

    @Test
    fun `mid term formula returns A for provided mu sigma and total`() {
        val result = calculator.calculateGrade(
            RelativeGradeCalculator.GradeInput(
                formula = RelativeGradeCalculator.TotalFormula.MIDTERM_TEE,
                cat1 = null,
                cat2 = null,
                midTerm = 85.0,
                tee = 50.0,
                attendance = 14.5,
                otherAssessment = 0.5,
                mean = 77.0,
                standardDeviation = 7.5
            )
        )

        assertTrue(result is RelativeGradeCalculator.Validation.Success)
        val gradeResult = (result as RelativeGradeCalculator.Validation.Success).result

        assertEquals(81.0, gradeResult.totalMarks, 0.00001)
        assertEquals("A", gradeResult.letterGrade)
        assertEquals(9, gradeResult.gradePoint)
    }

    @Test
    fun `tee below 40 gives F`() {
        val result = calculator.calculateGrade(
            RelativeGradeCalculator.GradeInput(
                formula = RelativeGradeCalculator.TotalFormula.CAT1_CAT2_TEE,
                cat1 = 45.0,
                cat2 = 45.0,
                midTerm = null,
                tee = 39.0,
                attendance = 15.0,
                otherAssessment = 10.0,
                mean = 77.0,
                standardDeviation = 7.5
            )
        )

        assertTrue(result is RelativeGradeCalculator.Validation.Success)
        val gradeResult = (result as RelativeGradeCalculator.Validation.Success).result
        assertEquals("F", gradeResult.letterGrade)
        assertEquals(0, gradeResult.gradePoint)
    }

    @Test
    fun `total below mu minus one point five sigma gives E when pass conditions are met`() {
        val result = calculator.calculateGrade(
            RelativeGradeCalculator.GradeInput(
                formula = RelativeGradeCalculator.TotalFormula.MIDTERM_TEE,
                cat1 = null,
                cat2 = null,
                midTerm = 80.0,
                tee = 45.0,
                attendance = 3.0,
                otherAssessment = 0.0,
                mean = 77.0,
                standardDeviation = 7.5
            )
        )

        assertTrue(result is RelativeGradeCalculator.Validation.Success)
        val gradeResult = (result as RelativeGradeCalculator.Validation.Success).result
        assertTrue(gradeResult.totalMarks < 65.75)
        assertEquals("E", gradeResult.letterGrade)
        assertEquals(5, gradeResult.gradePoint)
    }

    @Test
    fun `exact upper cutoff gives S`() {
        val result = calculateFromTotal(88.25)
        assertEquals("S", result.letterGrade)
        assertEquals(10, result.gradePoint)
    }

    @Test
    fun `just below S cutoff gives A`() {
        val result = calculateFromTotal(88.24)
        assertEquals("A", result.letterGrade)
        assertEquals(9, result.gradePoint)
    }

    @Test
    fun `exact A lower cutoff gives A`() {
        val result = calculateFromTotal(80.75)
        assertEquals("A", result.letterGrade)
        assertEquals(9, result.gradePoint)
    }

    @Test
    fun `just below A cutoff gives B`() {
        val result = calculateFromTotal(80.74)
        assertEquals("B", result.letterGrade)
        assertEquals(8, result.gradePoint)
    }

    @Test
    fun `exact B lower cutoff gives B`() {
        val result = calculateFromTotal(73.25)
        assertEquals("B", result.letterGrade)
        assertEquals(8, result.gradePoint)
    }

    @Test
    fun `just below B cutoff gives C`() {
        val result = calculateFromTotal(73.24)
        assertEquals("C", result.letterGrade)
        assertEquals(7, result.gradePoint)
    }

    @Test
    fun `exact C lower cutoff gives C`() {
        val result = calculateFromTotal(69.5)
        assertEquals("C", result.letterGrade)
        assertEquals(7, result.gradePoint)
    }

    @Test
    fun `just below C cutoff gives D`() {
        val result = calculateFromTotal(69.49)
        assertEquals("D", result.letterGrade)
        assertEquals(6, result.gradePoint)
    }

    @Test
    fun `exact D lower cutoff gives D`() {
        val result = calculateFromTotal(65.75)
        assertEquals("D", result.letterGrade)
        assertEquals(6, result.gradePoint)
    }

    @Test
    fun `just below D cutoff gives E`() {
        val result = calculateFromTotal(65.74)
        assertEquals("E", result.letterGrade)
        assertEquals(5, result.gradePoint)
    }

    private fun calculateFromTotal(total: Double): RelativeGradeCalculator.CalculationResult {
        val result = calculator.calculateGrade(
            RelativeGradeCalculator.GradeInput(
                formula = RelativeGradeCalculator.TotalFormula.MIDTERM_TEE,
                cat1 = null,
                cat2 = null,
                midTerm = 100.0,
                tee = 100.0,
                attendance = total - 90.0,
                otherAssessment = 0.0,
                mean = 77.0,
                standardDeviation = 7.5
            )
        )
        assertTrue(result is RelativeGradeCalculator.Validation.Success)
        return (result as RelativeGradeCalculator.Validation.Success).result
    }
}
