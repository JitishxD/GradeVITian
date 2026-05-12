package me.jitish.gradevitian.domain.model

data class CourseEntry(
    val id: Int = 0,
    val courseName: String = "",
    val credits: Int = 0,
    val grade: Grade = Grade.NONE
)

enum class Grade(
    val label: String,
    val gradePoint: Int?,
    val countsTowardGpa: Boolean
) {
    S("S", 10, true),
    A("A", 9, true),
    B("B", 8, true),
    C("C", 7, true),
    D("D", 6, true),
    E("E", 5, true),
    F("F", 0, true),
    N("N", 0, true),
    P("P", null, false),
    NONE("-", null, false);

    companion object {
        fun fromLabel(label: String): Grade {
            return entries.find { it.label.equals(label, ignoreCase = true) } ?: NONE
        }
    }
}

