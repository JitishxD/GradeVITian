package me.jitish.gradevitian.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.jitish.gradevitian.ui.screens.home.HomeScreen
import me.jitish.gradevitian.ui.theme.GradeVitianTheme

@Preview(showBackground = true, showSystemUi = true, name = "Home Light")
@Composable
fun HomeScreenLightPreview() {
    GradeVitianTheme(darkTheme = false, dynamicColor = false) {
        HomeScreen(
            onNavigateToGpa = {},
            onNavigateToCgpa = {},
            onNavigateToEstimator = {},
            onNavigateToAttendance = {},
            onNavigateToGradePredictor = {},
            onNavigateToHistory = {},
            onNavigateToProfile = {},
            onNavigateToSettings = {},
            viewModel = null
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home Dark")
@Composable
fun HomeScreenDarkPreview() {
    GradeVitianTheme(darkTheme = true, dynamicColor = false) {
        HomeScreen(
            onNavigateToGpa = {},
            onNavigateToCgpa = {},
            onNavigateToEstimator = {},
            onNavigateToAttendance = {},
            onNavigateToGradePredictor = {},
            onNavigateToHistory = {},
            onNavigateToProfile = {},
            onNavigateToSettings = {},
            viewModel = null
        )
    }
}


