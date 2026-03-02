package me.jitish.gradevitian.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import me.jitish.gradevitian.ui.screens.attendance.AttendanceScreen
import me.jitish.gradevitian.ui.screens.auth.AuthScreen
import me.jitish.gradevitian.ui.screens.auth.AuthViewModel
import me.jitish.gradevitian.ui.screens.cgpa.CgpaScreen
import me.jitish.gradevitian.ui.screens.estimator.EstimatorScreen
import me.jitish.gradevitian.ui.screens.gpa.GpaScreen
import me.jitish.gradevitian.ui.screens.gradepredictor.GradePredictorScreen
import me.jitish.gradevitian.ui.screens.history.HistoryScreen
import me.jitish.gradevitian.ui.screens.home.HomeScreen
import me.jitish.gradevitian.ui.screens.home.HomeViewModel
import me.jitish.gradevitian.ui.screens.profile.ProfileScreen
import me.jitish.gradevitian.ui.screens.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    pendingRecordHolder: PendingRecordHolder
) {
    // Compute start destination only once to prevent NavHost recreation
    val startDestination = remember {
        if (authViewModel.isAuthenticated.value) Screen.Home else Screen.Auth
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable<Screen.Auth> {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Auth) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Home> {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                onNavigateToGpa = { navController.navigate(Screen.GpaCalculator) },
                onNavigateToCgpa = { navController.navigate(Screen.CgpaCalculator) },
                onNavigateToEstimator = { navController.navigate(Screen.CgpaEstimator) },
                onNavigateToAttendance = { navController.navigate(Screen.AttendanceCalculator) },
                onNavigateToGradePredictor = { navController.navigate(Screen.GradePredictor) },
                onNavigateToHistory = { navController.navigate(Screen.History) },
                onNavigateToProfile = { navController.navigate(Screen.Profile) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) },
                viewModel = homeViewModel
            )
        }

        composable<Screen.GpaCalculator> {
            GpaScreen(onBack = { navController.popBackStack() })
        }

        composable<Screen.CgpaCalculator> {
            CgpaScreen(onBack = { navController.popBackStack() })
        }

        composable<Screen.CgpaEstimator> {
            EstimatorScreen(onBack = { navController.popBackStack() })
        }

        composable<Screen.AttendanceCalculator> {
            AttendanceScreen(onBack = { navController.popBackStack() })
        }

        composable<Screen.GradePredictor> {
            GradePredictorScreen(onBack = { navController.popBackStack() })
        }

        composable<Screen.History> {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onReloadGpa = { record ->
                    pendingRecordHolder.pendingGpaRecord = record
                    navController.navigate(Screen.GpaCalculator)
                },
                onReloadCgpa = { record ->
                    pendingRecordHolder.pendingCgpaRecord = record
                    navController.navigate(Screen.CgpaCalculator)
                }
            )
        }

        composable<Screen.Profile> {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Screen.Auth) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

