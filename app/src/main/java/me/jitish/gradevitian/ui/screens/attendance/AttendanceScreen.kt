package me.jitish.gradevitian.ui.screens.attendance

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import me.jitish.gradevitian.ui.components.AttendanceBar
import me.jitish.gradevitian.ui.components.GradeTopAppBar
import me.jitish.gradevitian.ui.components.ResultCard

@Composable
fun AttendanceScreen(
    onBack: () -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.saveMessage) {
        uiState.saveMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSaveMessage()
        }
    }

    Scaffold(
        topBar = { GradeTopAppBar(title = "Attendance Calculator", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val tabTitles = listOf("Simple", "Detailed")
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
                    0 -> SimpleTab(uiState, viewModel)
                    1 -> DetailedTab(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
private fun SimpleTab(uiState: AttendanceUiState, viewModel: AttendanceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter your classes present and absent to calculate attendance.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.simplePresent,
                        onValueChange = { viewModel.updateSimpleField("present", it) },
                        label = { Text("Classes Present") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = uiState.simpleAbsent,
                        onValueChange = { viewModel.updateSimpleField("absent", it) },
                        label = { Text("Classes Absent") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        ActionButtons(
            onCalculate = viewModel::calculateSimple,
            onReset = viewModel::resetSimple,
            onSave = viewModel::saveSimpleRecord
        )

        AnimatedVisibility(
            visible = uiState.simplePercentage != null,
            enter = fadeIn() + slideInVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AttendanceBar(percentage = uiState.simplePercentage ?: 0.0)
                ResultCard(
                    title = uiState.simpleResultMessage ?: "",
                    isError = uiState.simpleIsError
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.simpleIsError && uiState.simplePercentage == null,
            enter = fadeIn()
        ) {
            ResultCard(
                title = uiState.simpleResultMessage ?: "",
                isError = true
            )
        }

        // Note about 75% attendance
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Text(
                text = "Note: VIT requires a minimum 75% attendance to write exams. Stay above it!",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DetailedTab(uiState: AttendanceUiState, viewModel: AttendanceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter total classes and either present or absent count (not both).",
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
                    value = uiState.totalClasses,
                    onValueChange = { viewModel.updateDetailedField("total", it) },
                    label = { Text("Total No. of Classes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text("Enter one of:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.detailedPresent,
                        onValueChange = { viewModel.updateDetailedField("present", it) },
                        label = { Text("Present") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = uiState.detailedAbsent.isBlank()
                    )
                    OutlinedTextField(
                        value = uiState.detailedAbsent,
                        onValueChange = { viewModel.updateDetailedField("absent", it) },
                        label = { Text("Absent") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = uiState.detailedPresent.isBlank()
                    )
                }
            }
        }

        ActionButtons(
            onCalculate = viewModel::calculateDetailed,
            onReset = viewModel::resetDetailed
        )

        AnimatedVisibility(
            visible = uiState.detailedPercentage != null,
            enter = fadeIn() + slideInVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AttendanceBar(percentage = uiState.detailedPercentage ?: 0.0)
                ResultCard(
                    title = uiState.detailedResultMessage ?: "",
                    isError = uiState.detailedIsError
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.detailedIsError && uiState.detailedPercentage == null,
            enter = fadeIn()
        ) {
            ResultCard(
                title = uiState.detailedResultMessage ?: "",
                isError = true
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

