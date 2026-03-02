package me.jitish.gradevitian.ui.screens.estimator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.jitish.gradevitian.ui.components.ActionButtons
import me.jitish.gradevitian.ui.components.GradeTopAppBar
import me.jitish.gradevitian.ui.components.ResultCard

@Composable
fun EstimatorScreen(
    onBack: () -> Unit,
    viewModel: EstimatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { GradeTopAppBar(title = "CGPA Estimator", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Find the minimum GPA you need in your next semester to achieve your desired CGPA.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Target", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = uiState.desiredCgpa,
                        onValueChange = { viewModel.updateField("desired", it) },
                        label = { Text("Desired CGPA") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("Between 0 and 10") }
                    )

                    HorizontalDivider()
                    Text("Current Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.currentCgpa,
                            onValueChange = { viewModel.updateField("current", it) },
                            label = { Text("Current CGPA") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = uiState.completedCredits,
                            onValueChange = { viewModel.updateField("completed", it) },
                            label = { Text("Credits Done") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    HorizontalDivider()
                    Text("Next Semester", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = uiState.newCredits,
                        onValueChange = { viewModel.updateField("new", it) },
                        label = { Text("Credits Taken") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = { Text("Credits you're taking this semester") }
                    )
                }
            }

            ActionButtons(
                onCalculate = viewModel::calculate,
                onReset = viewModel::reset,
                calculateLabel = "Estimate"
            )

            AnimatedVisibility(
                visible = uiState.resultTitle != null,
                enter = fadeIn() + slideInVertically()
            ) {
                ResultCard(
                    title = uiState.resultTitle ?: "",
                    subtitle = uiState.resultSubtitle ?: "",
                    isError = uiState.isError
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

