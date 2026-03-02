package me.jitish.gradevitian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.jitish.gradevitian.ui.navigation.AppNavHost
import me.jitish.gradevitian.ui.navigation.PendingRecordHolder
import me.jitish.gradevitian.ui.screens.home.HomeScreen
import me.jitish.gradevitian.ui.screens.settings.SettingsViewModel
import me.jitish.gradevitian.ui.theme.GradeVitianTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var pendingRecordHolder: PendingRecordHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.uiState.collectAsState()

            GradeVitianTheme(
                darkTheme = settings.darkMode,
                dynamicColor = settings.dynamicColor
            ) {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    pendingRecordHolder = pendingRecordHolder
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home Screen Light")
@Composable
fun HomeScreenPreviewLight() {
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

@Preview(showBackground = true, showSystemUi = true, name = "Home Screen Dark")
@Composable
fun HomeScreenPreviewDark() {
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

