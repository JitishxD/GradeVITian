package me.jitish.gradevitian.ui.screens.gradepredictor

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.jitish.gradevitian.ui.components.ActionButtons
import me.jitish.gradevitian.ui.components.GradeTopAppBar
import me.jitish.gradevitian.ui.components.ResultCard

@Composable
fun GradePredictorScreen(
    onBack: () -> Unit,
    viewModel: GradePredictorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { GradeTopAppBar(title = "Grade Predictor", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val tabTitles = listOf("Grade Predictor", "Weightage Converter")
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } }
                    ) {
                        Text(title, modifier = Modifier.padding(16.dp))
                    }
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> PredictorTab(uiState, viewModel)
                    1 -> WeightageTab(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
private fun PredictorTab(uiState: GradePredictorUiState, viewModel: GradePredictorViewModel) {
    val hasTheory = (uiState.theoryCredits.toIntOrNull() ?: 0) > 0
    val hasLab = (uiState.labCredits.toIntOrNull() ?: 0) > 0
    val hasJComp = (uiState.jCompCredits.toIntOrNull() ?: 0) > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter your marks to predict your grade (Absolute grading).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Credits Section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Course Credits", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.courseCredits,
                        onValueChange = { viewModel.updateField("courseCredits", it) },
                        label = { Text("Total") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = uiState.theoryCredits,
                        onValueChange = { viewModel.updateField("theoryCredits", it) },
                        label = { Text("Theory") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.labCredits,
                        onValueChange = { viewModel.updateField("labCredits", it) },
                        label = { Text("Lab") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = uiState.jCompCredits,
                        onValueChange = { viewModel.updateField("jCompCredits", it) },
                        label = { Text("J-Comp") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        // Theory Section
        AnimatedVisibility(visible = hasTheory) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Theory Component", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.cat1,
                            onValueChange = { viewModel.updateField("cat1", it) },
                            label = { Text("CAT-1 (/50)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = uiState.cat2,
                            onValueChange = { viewModel.updateField("cat2", it) },
                            label = { Text("CAT-2 (/50)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.da1,
                            onValueChange = { viewModel.updateField("da1", it) },
                            label = { Text("DA-1") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = uiState.da2,
                            onValueChange = { viewModel.updateField("da2", it) },
                            label = { Text("DA-2") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = uiState.da3,
                            onValueChange = { viewModel.updateField("da3", it) },
                            label = { Text("DA-3") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                    OutlinedTextField(
                        value = uiState.theoryFat,
                        onValueChange = { viewModel.updateField("theoryFat", it) },
                        label = { Text("Theory FAT (/100)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = uiState.additionalLearning,
                        onValueChange = { viewModel.updateField("additionalLearning", it) },
                        label = { Text("Additional Learning (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        }

        // Lab Section
        AnimatedVisibility(visible = hasLab) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Lab Component", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.labInternal,
                            onValueChange = { viewModel.updateField("labInternal", it) },
                            label = { Text("Lab Internal (/60)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = uiState.labFat,
                            onValueChange = { viewModel.updateField("labFat", it) },
                            label = { Text("Lab FAT (/50)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            }
        }

        // J-Component Section
        AnimatedVisibility(visible = hasJComp) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("J-Component", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.review1,
                            onValueChange = { viewModel.updateField("review1", it) },
                            label = { Text("Review 1") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = uiState.review2,
                            onValueChange = { viewModel.updateField("review2", it) },
                            label = { Text("Review 2") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = uiState.review3,
                            onValueChange = { viewModel.updateField("review3", it) },
                            label = { Text("Review 3") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            }
        }

        ActionButtons(
            onCalculate = viewModel::predict,
            onReset = viewModel::reset,
            calculateLabel = "Predict"
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

@Composable
private fun WeightageTab(uiState: GradePredictorUiState, viewModel: GradePredictorViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Convert marks from one scale to another.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.maxOriginal,
                    onValueChange = { viewModel.updateField("maxOriginal", it) },
                    label = { Text("Maximum Original Marks") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = uiState.maxWeightage,
                    onValueChange = { viewModel.updateField("maxWeightage", it) },
                    label = { Text("Maximum Weightage Marks") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                HorizontalDivider()
                OutlinedTextField(
                    value = uiState.obtainedOriginal,
                    onValueChange = { viewModel.updateField("obtainedOriginal", it) },
                    label = { Text("Obtained Original Marks") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }

        ActionButtons(
            onCalculate = viewModel::convertWeightage,
            onReset = viewModel::resetWeightage,
            calculateLabel = "Convert"
        )

        AnimatedVisibility(
            visible = uiState.weightageResult != null,
            enter = fadeIn() + slideInVertically()
        ) {
            ResultCard(title = uiState.weightageResult ?: "")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

