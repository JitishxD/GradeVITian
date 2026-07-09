package me.jitish.gradevitian.ui.screens.gradecalculator

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun GradeCalculatorScreen(
    onBack: () -> Unit,
    viewModel: GradeCalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setMode(
            if (pagerState.currentPage == 0) GradeCalcMode.DIRECT_MU_SIGMA else GradeCalcMode.CLASS_MARKS
        )
    }

    Scaffold(
        topBar = { GradeTopAppBar(title = "Grade Calculator", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val tabTitles = listOf("Direct μ/σ", "Class Marks")
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
                    0 -> DirectMuSigmaTab(uiState, viewModel)
                    1 -> ClassMarksTab(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
private fun BaseMarksCard(uiState: GradeCalculatorUiState, viewModel: GradeCalculatorViewModel) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                if (uiState.useMidTermFormula) "Formula 2 Inputs" else "Formula 1 Inputs",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (uiState.useMidTermFormula) {
                OutlinedTextField(
                    value = uiState.midTerm,
                    onValueChange = { viewModel.updateField("midTerm", it) },
                    label = { Text("Mid Term (/100)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            } else {
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
            }
            OutlinedTextField(
                value = uiState.tee,
                onValueChange = { viewModel.updateField("tee", it) },
                label = { Text("TEE (/100)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.attendance,
                    onValueChange = { viewModel.updateField("attendance", it) },
                    label = { Text("Attendance (internal)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = uiState.otherAssessment,
                    onValueChange = { viewModel.updateField("otherAssessment", it) },
                    label = { Text("Other Assessment (internal)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
    }
}

@Composable
private fun ClassMarksTab(uiState: GradeCalculatorUiState, viewModel: GradeCalculatorViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (uiState.useMidTermFormula) {
                "Using Formula 2 from Settings:\nTotal = 0.6Mid Term + 0.3TEE + Attendance + Other Assessment.\nEnter class final totals from this same formula."
            } else {
                "Using Formula 1 from Settings:\nTotal = 0.3CAT1 + 0.3CAT2 + 0.3TEE + Attendance + Other Assessment.\nEnter class final totals from this same formula."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BaseMarksCard(uiState, viewModel)

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Class Distribution", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = uiState.classTotalsRaw,
                    onValueChange = { viewModel.updateField("classTotalsRaw", it) },
                    label = { Text("Class final totals (comma/space/newline)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
        }

        ActionButtons(onCalculate = viewModel::calculate, onReset = viewModel::reset)

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
private fun DirectMuSigmaTab(uiState: GradeCalculatorUiState, viewModel: GradeCalculatorViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (uiState.useMidTermFormula) {
                "Using Formula 2 from Settings:\n Total = 0.6Mid Term + 0.3TEE + Attendance + Other Assessment.\nEnter mu and sigma for class final totals from this same formula."
            } else {
                "Using Formula 1 from Settings:\n Total = 0.3CAT1 + 0.3CAT2 + 0.3TEE + Attendance + Other Assessment.\nEnter mu and sigma for class final totals from this same formula."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BaseMarksCard(uiState, viewModel)

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Class Metrics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.meanInput,
                        onValueChange = { viewModel.updateField("meanInput", it) },
                        label = { Text("Class Average (μ)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = uiState.sigmaInput,
                        onValueChange = { viewModel.updateField("sigmaInput", it) },
                        label = { Text("Std. Deviation (σ)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        }

        ActionButtons(onCalculate = viewModel::calculate, onReset = viewModel::reset)

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
