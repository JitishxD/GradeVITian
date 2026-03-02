package me.jitish.gradevitian.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Auth : Screen
    @Serializable data object Home : Screen
    @Serializable data object GpaCalculator : Screen
    @Serializable data object CgpaCalculator : Screen
    @Serializable data object CgpaEstimator : Screen
    @Serializable data object AttendanceCalculator : Screen
    @Serializable data object GradePredictor : Screen
    @Serializable data object History : Screen
    @Serializable data object Profile : Screen
    @Serializable data object Settings : Screen
}

