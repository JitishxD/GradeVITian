package me.jitish.gradevitian.domain.calculator

import javax.inject.Inject
import kotlin.math.sqrt

class RelativeGradeCalculator @Inject constructor() {

    enum class TotalFormula {
        CAT1_CAT2_TEE,
        MIDTERM_TEE
    }

    data class GradeInput(
        val formula: TotalFormula,
        val cat1: Double?,
        val cat2: Double?,
        val midTerm: Double?,
        val tee: Double,
        val attendance: Double,
        val otherAssessment: Double,
        val mean: Double,
        val standardDeviation: Double
    )

    data class CalculationResult(
        val totalMarks: Double,
        val mean: Double,
        val standardDeviation: Double,
        val letterGrade: String,
        val gradePoint: Int
    )

    sealed class Validation {
        data class Success(val result: CalculationResult) : Validation()
        data class Error(val message: String) : Validation()
    }

    fun calculateMeanAndStdDev(classTotals: List<Double>): Pair<Double, Double> {
        val mean = classTotals.average()
        val variance = classTotals.map { (it - mean) * (it - mean) }.average()
        return mean to sqrt(variance)
    }

    fun calculateGrade(input: GradeInput): Validation {
        val weightedPart = when (input.formula) {
            TotalFormula.CAT1_CAT2_TEE -> {
                val cat1 = input.cat1 ?: return Validation.Error("CAT-1 mark is required.")
                val cat2 = input.cat2 ?: return Validation.Error("CAT-2 mark is required.")
                (0.3 * cat1) + (0.3 * cat2) + (0.3 * input.tee)
            }

            TotalFormula.MIDTERM_TEE -> {
                val midTerm = input.midTerm ?: return Validation.Error("Mid Term mark is required.")
                (0.6 * midTerm) + (0.3 * input.tee)
            }
        }

        val total = weightedPart + input.attendance + input.otherAssessment
        val teePercentage = input.tee
        val totalPercentage = total

        if (teePercentage < 40.0 || totalPercentage < 40.0) {
            return Validation.Success(
                CalculationResult(
                    totalMarks = total,
                    mean = input.mean,
                    standardDeviation = input.standardDeviation,
                    letterGrade = "F",
                    gradePoint = 0
                )
            )
        }

        val grade = when {
            total >= input.mean + (1.5 * input.standardDeviation) -> "S" to 10
            total >= input.mean + (0.5 * input.standardDeviation) -> "A" to 9
            total >= input.mean - (0.5 * input.standardDeviation) -> "B" to 8
            total >= input.mean - input.standardDeviation -> "C" to 7
            total >= input.mean - (1.5 * input.standardDeviation) -> "D" to 6
            else -> "E" to 5
        }

        return Validation.Success(
            CalculationResult(
                totalMarks = total,
                mean = input.mean,
                standardDeviation = input.standardDeviation,
                letterGrade = grade.first,
                gradePoint = grade.second
            )
        )
    }
}
