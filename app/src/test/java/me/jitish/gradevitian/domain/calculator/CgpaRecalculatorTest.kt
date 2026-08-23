package me.jitish.gradevitian.domain.calculator

import me.jitish.gradevitian.domain.model.Grade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CgpaRecalculatorTest {

    private val calculator = CgpaRecalculator()

    @Test
    fun `recalculates CGPA when one course improves from C to B`() {
        // CGPA 8.5 over 20 credits; one 3-credit course C(7) → B(8): +3 points → 173/20 = 8.65
        val result = calculator.recalculateFromGradeChange(
            currentCgpa = 8.5,
            totalCredits = 20,
            courseCredits = 3,
            oldGrade = Grade.C,
            newGrade = Grade.B
        )

        assertTrue(result is CgpaRecalculator.RecalculationValidation.Success)
        val recalc = (result as CgpaRecalculator.RecalculationValidation.Success).result
        assertEquals(8.5, recalc.oldCgpa, 0.00001)
        assertEquals(8.65, recalc.newCgpa, 0.00001)
        assertEquals(0.15, recalc.delta, 0.00001)
    }

    @Test
    fun `returns error when course credits exceed total credits`() {
        val result = calculator.recalculateFromGradeChange(
            currentCgpa = 8.0,
            totalCredits = 10,
            courseCredits = 15,
            oldGrade = Grade.C,
            newGrade = Grade.B
        )

        assertTrue(result is CgpaRecalculator.RecalculationValidation.Error)
    }

    @Test
    fun `no change when old and new grades are equal`() {
        val result = calculator.recalculateFromGradeChange(
            currentCgpa = 8.0,
            totalCredits = 20,
            courseCredits = 3,
            oldGrade = Grade.C,
            newGrade = Grade.C
        )

        assertTrue(result is CgpaRecalculator.RecalculationValidation.Success)
        val recalc = (result as CgpaRecalculator.RecalculationValidation.Success).result
        assertEquals(8.0, recalc.newCgpa, 0.00001)
        assertEquals(0.0, recalc.delta, 0.00001)
    }

    @Test
    fun `rejects pass-fail grade`() {
        val result = calculator.recalculateFromGradeChange(
            currentCgpa = 8.0,
            totalCredits = 20,
            courseCredits = 3,
            oldGrade = Grade.P,
            newGrade = Grade.B
        )

        assertTrue(result is CgpaRecalculator.RecalculationValidation.Error)
    }
}
