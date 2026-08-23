package me.jitish.gradevitian.ui.screens.cgparecalc

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
import me.jitish.gradevitian.ui.components.DropdownSelector
import me.jitish.gradevitian.ui.components.GradeTopAppBar
import me.jitish.gradevitian.ui.components.ResultCard
import me.jitish.gradevitian.ui.components.boldTextParts

@Composable
fun CgpaRecalculatorScreen(
    onBack: () -> Unit,
    viewModel: CgpaRecalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { GradeTopAppBar(title = "CGPA Recalculator", onBack = onBack) }
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
                text = boldTextParts(
                    text = "See how improving one course grade (e.g. C → B) affects your CGPA. \nEnter graded credits only; exclude P/pass-fail.",
                    "P/pass-fail"
                ),
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
                    Text("Current Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.currentCgpa,
                            onValueChange = { viewModel.updateField("currentCgpa", it) },
                            label = { Text("Current CGPA") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = uiState.totalCredits,
                            onValueChange = { viewModel.updateField("totalCredits", it) },
                            label = { Text("Total Credits") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            supportingText = { Text("All semesters") }
                        )
                    }

                    HorizontalDivider()
                    Text(
                        "Improved Course",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = uiState.courseCredits,
                        onValueChange = { if (it.length <= 2) viewModel.updateField("courseCredits", it) },
                        label = { Text("Course Credits") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DropdownSelector(
                            label = "Old Grade",
                            options = GRADE_OPTIONS,
                            selected = uiState.oldGrade,
                            onSelected = { viewModel.updateField("oldGrade", it) },
                            modifier = Modifier.weight(1f)
                        )
                        DropdownSelector(
                            label = "New Grade",
                            options = GRADE_OPTIONS,
                            selected = uiState.newGrade,
                            onSelected = { viewModel.updateField("newGrade", it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            ActionButtons(
                onCalculate = viewModel::calculate,
                onReset = viewModel::reset
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
