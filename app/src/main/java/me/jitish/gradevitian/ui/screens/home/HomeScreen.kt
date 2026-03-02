package me.jitish.gradevitian.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToGpa: () -> Unit,
    onNavigateToCgpa: () -> Unit,
    onNavigateToEstimator: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToGradePredictor: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel? = null
) {
    val userName by (viewModel?.userName ?: kotlinx.coroutines.flow.MutableStateFlow<String?>(null)).collectAsState()
    val greeting = if (!userName.isNullOrBlank()) "Hi, ${userName!!.split(" ").first()}! 👋" else "Hi there! 👋"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "gradeVITian",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "I'll help you in computing your GPA, CGPA, attendance and more.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Calculators",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                FeatureCard(
                    title = "GPA\nCalculator",
                    subtitle = "Semester GPA",
                    icon = Icons.Default.Calculate,
                    onClick = onNavigateToGpa
                )
            }

            item {
                FeatureCard(
                    title = "CGPA\nCalculator",
                    subtitle = "Cumulative GPA",
                    icon = Icons.Default.Functions,
                    onClick = onNavigateToCgpa
                )
            }

            item {
                FeatureCard(
                    title = "CGPA\nEstimator",
                    subtitle = "Target GPA",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    onClick = onNavigateToEstimator
                )
            }

            item {
                FeatureCard(
                    title = "Attendance\nCalculator",
                    subtitle = "Track attendance",
                    icon = Icons.Default.EventAvailable,
                    onClick = onNavigateToAttendance
                )
            }

            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }

            item {
                FeatureCard(
                    title = "Grade\nPredictor",
                    subtitle = "Predict grades",
                    icon = Icons.Default.School,
                    onClick = onNavigateToGradePredictor
                )
            }

            item {
                FeatureCard(
                    title = "Saved\nHistory",
                    subtitle = "Past calculations",
                    icon = Icons.Default.History,
                    onClick = onNavigateToHistory
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

