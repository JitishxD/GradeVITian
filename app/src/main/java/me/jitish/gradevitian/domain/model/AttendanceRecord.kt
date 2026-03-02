package me.jitish.gradevitian.domain.model

data class AttendanceRecord(
    val id: String = "",
    val courseName: String = "",
    val attendancePercentage: Double = 0.0,
    val classesPresent: Int = 0,
    val classesAbsent: Int = 0,
    val totalClasses: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

