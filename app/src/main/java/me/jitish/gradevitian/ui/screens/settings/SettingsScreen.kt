package me.jitish.gradevitian.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.jitish.gradevitian.ui.components.GradeTopAppBar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { GradeTopAppBar(title = "Settings", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Dark Mode") },
                        supportingContent = { Text("Use dark theme throughout the app") },
                        trailingContent = {
                            Switch(
                                checked = uiState.darkMode,
                                onCheckedChange = viewModel::toggleDarkMode
                            )
                        }
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Dynamic Colors") },
                            supportingContent = { Text("Match theme to your wallpaper (Android 12+)") },
                            trailingContent = {
                                Switch(
                                    checked = uiState.dynamicColor,
                                    onCheckedChange = viewModel::toggleDynamicColor
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("gradeVITian") },
                        supportingContent = { Text("Your academic companion for VIT") }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Version") },
                        supportingContent = { Text("1.0.0") }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Developer") },
                        supportingContent = { Text("Built with ❤\uFE0F for VITians") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Text(
                    text = "Data is synced to Firebase when signed in. Calculations work offline too!",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

