package me.jitish.gradevitian.ui.screens.cgpa

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
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
fun CgpaScreen(
    onBack: () -> Unit,
    viewModel: CgpaViewModel = hiltViewModel()
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
        topBar = { GradeTopAppBar(title = "CGPA Calculator", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (pagerState.currentPage == 0) {
                FloatingActionButton(onClick = viewModel::addSemester) {
                    Icon(Icons.Default.Add, contentDescription = "Add Semester")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val tabTitles = listOf("Semester-wise", "Instant CGPA")
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
                    0 -> SemesterWiseTab(uiState, viewModel)
                    1 -> InstantCgpaTab(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
private fun SemesterWiseTab(uiState: CgpaUiState, viewModel: CgpaViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Enter your semester-wise credits and GPA to calculate CGPA.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        itemsIndexed(
            items = uiState.semesters,
            key = { _, sem -> sem.semesterNumber }
        ) { _, semester ->
            SemesterRow(
                semesterNumber = semester.semesterNumber,
                credits = uiState.semCreditsText[semester.semesterNumber] ?: "",
                gpa = uiState.semGpaText[semester.semesterNumber] ?: "",
                onCreditsChange = { text ->
                    viewModel.updateSemCredits(semester.semesterNumber, text)
                },
                onGpaChange = { text ->
                    viewModel.updateSemGpa(semester.semesterNumber, text)
                },
                onRemove = if (uiState.semesters.size > 1) {
                    { viewModel.removeSemester(semester.semesterNumber) }
                } else null
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            ActionButtons(
                onCalculate = viewModel::calculateSemesterWise,
                onReset = viewModel::resetSemesterWise,
                onSave = viewModel::saveRecord
            )
        }

        item {
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
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun InstantCgpaTab(uiState: CgpaUiState, viewModel: CgpaViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Quickly calculate your new CGPA after the current semester.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Current Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.currentCgpa,
                            onValueChange = { viewModel.updateInstantField("cgpa", it) },
                            label = { Text("Current CGPA") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = uiState.completedCredits,
                            onValueChange = { viewModel.updateInstantField("completedCredits", it) },
                            label = { Text("Completed Credits") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    HorizontalDivider()
                    Text("This Semester", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.currentSemGpa,
                            onValueChange = { viewModel.updateInstantField("semGpa", it) },
                            label = { Text("This Sem GPA") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = uiState.currentSemCredits,
                            onValueChange = { viewModel.updateInstantField("semCredits", it) },
                            label = { Text("This Sem Credits") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        }

        item {
            ActionButtons(
                onCalculate = viewModel::calculateInstant,
                onReset = viewModel::resetInstant
            )
        }

        item {
            AnimatedVisibility(
                visible = uiState.instantResultTitle != null,
                enter = fadeIn() + slideInVertically()
            ) {
                ResultCard(
                    title = uiState.instantResultTitle ?: "",
                    subtitle = uiState.instantResultSubtitle ?: "",
                    isError = uiState.instantIsError
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SemesterRow(
    semesterNumber: Int,
    credits: String,
    gpa: String,
    onCreditsChange: (String) -> Unit,
    onGpaChange: (String) -> Unit,
    onRemove: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Sem $semesterNumber",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(52.dp)
            )
            OutlinedTextField(
                value = credits,
                onValueChange = { if (it.length <= 2) onCreditsChange(it) },
                label = { Text("Credits") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = gpa,
                onValueChange = { if (it.length <= 5) onGpaChange(it) },
                label = { Text("GPA") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

