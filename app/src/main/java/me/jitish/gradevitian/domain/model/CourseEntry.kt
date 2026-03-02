package me.jitish.gradevitian.domain.model

data class CourseEntry(
    val id: Int = 0,
    val courseName: String = "",
    val credits: Int = 0,
    val grade: Grade = Grade.NONE
)

enum class Grade(val label: String, val gradePoint: Int) {
    S("S", 10),
    A("A", 9),
    B("B", 8),
    C("C", 7),
    D("D", 6),
    E("E", 5),
    F("F", 0),
    N("N", 0),
    NONE("-", 0);

    companion object {
        fun fromLabel(label: String): Grade {
            return entries.find { it.label.equals(label, ignoreCase = true) } ?: NONE
        }
    }
}

