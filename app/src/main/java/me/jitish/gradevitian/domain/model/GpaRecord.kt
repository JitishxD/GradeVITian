package me.jitish.gradevitian.domain.model

data class GpaRecord(
    val id: String = "",
    val semesterName: String = "",
    val courses: List<CourseEntry> = emptyList(),
    val gpa: Double = 0.0,
    val totalCredits: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

