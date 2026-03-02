package me.jitish.gradevitian.domain.calculator

import me.jitish.gradevitian.domain.model.Grade
import javax.inject.Inject
import kotlin.math.ceil

/**
 * Grade Predictor - matches referenceWebCode/Grade Predictor.js exactly.
 *
 * Theory component:
 *   cat1converted = (cat1 / 50) × 15
 *   cat2converted = (cat2 / 50) × 15
 *   da1c, da2c, da3c pass-through
 *   theoryFatConverted = (theoryFat × 40) / 100
 *   theoryTotal = theoryFatConverted + cat1c + cat2c + da1 + da2 + da3
 *   If internal marks (tcm + additionalLearning - theoryFatConverted) >= 60 → cap at 60 + theoryFatConverted
 *   theoryWeighted = theoryTotal × (courseCredits - labCredits - jCredits) / courseCredits
 *
 * Lab component:
 *   labFatConverted = (labFat × 40) / 50
 *   labTotal = labInternal + labFatConverted
 *   labWeighted = labTotal × (courseCredits - theoryCredits - jCredits) / courseCredits
 *
 * J-Component:
 *   jTotal = review1 + review2 + review3
 *   jWeighted = jTotal × (courseCredits - theoryCredits - labCredits) / courseCredits
 *
 * Final = theoryWeighted + labWeighted + jWeighted
 *
 * Grade assignment (absolute grading):
 *   >= 90 → S, >= 80 → A, >= 70 → B, >= 60 → C, >= 55 → D, >= 50 → E, < 50 → F
 */
class GradePredictor @Inject constructor() {

    data class PredictionInput(
        val courseCredits: Int = 0,
        val theoryCredits: Int = 0,
        val labCredits: Int = 0,
        val jCompCredits: Int = 0,
        // Theory
        val cat1: Double? = null,
        val cat2: Double? = null,
        val da1: Double? = null,
        val da2: Double? = null,
        val da3: Double? = null,
        val theoryFat: Double? = null,
        val additionalLearning: Double? = null,
        // Lab
        val labInternal: Double? = null,
        val labFat: Double? = null,
        // J-Component
        val review1: Double? = null,
        val review2: Double? = null,
        val review3: Double? = null
    )

    data class PredictionResult(
        val totalMarks: Double,
        val roundedMarks: Int,
        val predictedGrade: Grade,
        val message: String
    )

    sealed class PredictionValidation {
        data class Success(val result: PredictionResult) : PredictionValidation()
        data class Error(val message: String, val detail: String = "") : PredictionValidation()
    }

    /**
     * Weightage Converter - matches weightageconv() in JS
     */
    fun convertWeightage(maxOriginal: Double, maxWeightage: Double, obtainedOriginal: Double): String {
        if (maxOriginal == 0.0 || maxWeightage == 0.0 || obtainedOriginal == 0.0) {
            return "Kindly check all the entries filled are valid and non-zeros."
        }
        if (obtainedOriginal > maxOriginal) {
            return "Your obtained original marks shouldn't be greater than maximum original marks."
        }
        val result = (obtainedOriginal / maxOriginal) * maxWeightage
        return "Your obtained weightage mark is ${String.format("%.2f", result)}. You have secured ${String.format("%.2f", result)} out of ${maxWeightage.toInt()}."
    }

    fun predict(input: PredictionInput): PredictionValidation {
        val cc = input.courseCredits
        val tc = input.theoryCredits
        val lc = input.labCredits
        val jc = input.jCompCredits

        if (cc <= 0) {
            return PredictionValidation.Error("Please select Total Course Credits for the course.")
        }
        if (tc + lc + jc == 0) {
            return PredictionValidation.Error(
                "Please select the respective individual component credits (Theory, Lab or J-comp) for the course you have chosen."
            )
        }
        if (tc + lc + jc != cc) {
            return PredictionValidation.Error(
                "Your Total Course Credits are not matched with the selected component credits. (Theory or Lab or J-comp)"
            )
        }

        // Validate theory entries
        if (tc > 0) {
            val theoryFields = listOf(input.cat1, input.cat2, input.da1, input.da2, input.da3, input.theoryFat)
            if (theoryFields.any { it == null }) {
                return PredictionValidation.Error("All entries are not filled in the Theory component.")
            }
        }

        // Validate lab entries
        if (lc > 0) {
            if (input.labInternal == null || input.labFat == null) {
                return PredictionValidation.Error("All entries are not filled in the Lab component.")
            }
        }

        // Validate j-component entries
        if (jc > 0) {
            if (input.review1 == null || input.review2 == null || input.review3 == null) {
                return PredictionValidation.Error("All entries are not filled in the J-component.")
            }
        }

        // Calculate theory
        var tcmadd = 0.0
        if (tc > 0) {
            val cat1 = input.cat1!!
            val cat2 = input.cat2!!
            val da1 = input.da1!!
            val da2 = input.da2!!
            val da3 = input.da3!!
            val tfat = input.theoryFat!!
            val addLearn = input.additionalLearning ?: 0.0

            if (tfat < 40) {
                return PredictionValidation.Error(
                    "Oops! You got failed in this course.",
                    "You got less than 40 marks in Theory FAT. If this course is graded under Absolute grading, your grade is 'F'."
                )
            }

            val cat1c = (cat1 / 50.0) * 15.0
            val cat2c = (cat2 / 50.0) * 15.0
            val tfatc = (tfat * 40.0) / 100.0
            val tcm = tfatc + cat1c + cat2c + da1 + da2 + da3

            tcmadd = if ((tcm + addLearn - tfatc) >= 60.0) {
                ((60.0 + tfatc) * (cc - lc - jc)) / cc
            } else {
                ((tcm + addLearn) * (cc - lc - jc)) / cc
            }
        }

        // Calculate lab
        var lcmadd = 0.0
        if (lc > 0) {
            val labInternal = input.labInternal!!
            val labFatConverted = (input.labFat!! * 40.0) / 50.0
            val labTotal = labInternal + labFatConverted

            if (labTotal < 50) {
                return PredictionValidation.Error(
                    "Oops! You got failed in this course.",
                    "You got less than 50 marks in Lab Component (Internals + FAT). If this course is graded under Absolute grading, your grade is 'F'."
                )
            }
            lcmadd = (labTotal * (cc - tc - jc)) / cc
        }

        // Calculate j-component
        var jcmadd = 0.0
        if (jc > 0) {
            val jTotal = input.review1!! + input.review2!! + input.review3!!
            if (jTotal < 50) {
                return PredictionValidation.Error(
                    "Oops! You got failed in this course.",
                    "You got less than 50 marks in J-Component (Review-1 + Review-2 + Review-3). If this course is graded under Absolute grading, your grade is 'F'."
                )
            }
            jcmadd = (jTotal * (cc - tc - lc)) / cc
        }

        val totalMarks = tcmadd + lcmadd + jcmadd

        // Check overall fail
        if (tc > 0 && (input.theoryFat ?: 0.0) >= 40 && totalMarks < 50) {
            return PredictionValidation.Error(
                "Oops! You got failed in this course because your Total Final Marks (Theory + Lab + J-comp) are less than 50% of total (out of 100).",
                "If this course is graded under Absolute grading, your grade is 'F'."
            )
        }

        val grade = when {
            totalMarks >= 90 -> Grade.S
            totalMarks >= 80 -> Grade.A
            totalMarks >= 70 -> Grade.B
            totalMarks >= 60 -> Grade.C
            totalMarks >= 55 -> Grade.D
            totalMarks >= 50 -> Grade.E
            else -> Grade.F
        }

        return PredictionValidation.Success(
            PredictionResult(
                totalMarks = String.format("%.4f", totalMarks).toDouble(),
                roundedMarks = ceil(totalMarks).toInt(),
                predictedGrade = grade,
                message = "If this course is graded under Absolute grading, your grade is '${grade.label}'."
            )
        )
    }
}

