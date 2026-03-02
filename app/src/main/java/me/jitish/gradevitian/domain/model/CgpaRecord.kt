package me.jitish.gradevitian.domain.model

data class CgpaRecord(
    val id: String = "",
    val semesters: List<SemesterEntry> = emptyList(),
    val cgpa: Double = 0.0,
    val totalCredits: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

