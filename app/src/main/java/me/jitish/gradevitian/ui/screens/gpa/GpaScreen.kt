package me.jitish.gradevitian.ui.screens.gpa

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.jitish.gradevitian.domain.model.Grade
import me.jitish.gradevitian.ui.components.ActionButtons
import me.jitish.gradevitian.ui.components.DropdownSelector
import me.jitish.gradevitian.ui.components.GradeTopAppBar
import me.jitish.gradevitian.ui.components.ResultCard

@Composable
fun GpaScreen(
    onBack: () -> Unit,
    viewModel: GpaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveMessage) {
        uiState.saveMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSaveMessage()
        }
    }

    Scaffold(
        topBar = { GradeTopAppBar(title = "GPA Calculator", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::addCourse) {
                Icon(Icons.Default.Add, contentDescription = "Add Course")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter your course credits and grades to calculate your GPA.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            itemsIndexed(
                items = uiState.courses,
                key = { _, course -> course.id }
            ) { index, course ->
                CourseRow(
                    index = index + 1,
                    credits = if (course.credits > 0) course.credits.toString() else "",
                    selectedGrade = if (course.grade != Grade.NONE) course.grade.label else "-",
                    onCreditsChange = { text ->
                        val credits = text.toIntOrNull() ?: 0
                        viewModel.updateCredit(course.id, credits)
                    },
                    onGradeSelected = { label ->
                        viewModel.updateGrade(course.id, Grade.fromLabel(label))
                    },
                    onRemove = if (uiState.courses.size > 1) {
                        { viewModel.removeCourse(course.id) }
                    } else null
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                ActionButtons(
                    onCalculate = viewModel::calculate,
                    onReset = viewModel::reset,
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
}

@Composable
private fun CourseRow(
    index: Int,
    credits: String,
    selectedGrade: String,
    onCreditsChange: (String) -> Unit,
    onGradeSelected: (String) -> Unit,
    onRemove: (() -> Unit)?
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
                text = "$index.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp)
            )

            OutlinedTextField(
                value = credits,
                onValueChange = { if (it.length <= 2) onCreditsChange(it) },
                label = { Text("Credits") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            DropdownSelector(
                label = "Grade",
                options = listOf("-", "S", "A", "B", "C", "D", "E", "F", "N"),
                selected = selectedGrade,
                onSelected = onGradeSelected,
                modifier = Modifier.weight(1f)
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

