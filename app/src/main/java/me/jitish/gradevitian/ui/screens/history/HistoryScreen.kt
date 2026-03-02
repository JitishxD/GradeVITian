package me.jitish.gradevitian.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.jitish.gradevitian.ui.components.GradeTopAppBar
import me.jitish.gradevitian.ui.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onReloadGpa: (me.jitish.gradevitian.domain.model.GpaRecord) -> Unit = {},
    onReloadCgpa: (me.jitish.gradevitian.domain.model.CgpaRecord) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { GradeTopAppBar(title = "Saved History", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!uiState.isAuthenticated) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.height(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sign in to view your saved records",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Your calculations are synced with Firebase when you're signed in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                val tabTitles = listOf("GPA", "CGPA", "Attendance")
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

                if (uiState.isLoading) {
                    LoadingIndicator(modifier = Modifier.padding(32.dp))
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> GpaRecordsList(uiState.gpaRecords, viewModel::deleteGpaRecord, onReloadGpa)
                            1 -> CgpaRecordsList(uiState.cgpaRecords, viewModel::deleteCgpaRecord, onReloadCgpa)
                            2 -> AttendanceRecordsList(uiState.attendanceRecords, viewModel::deleteAttendanceRecord)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GpaRecordsList(
    records: List<me.jitish.gradevitian.domain.model.GpaRecord>,
    onDelete: (String) -> Unit,
    onReload: (me.jitish.gradevitian.domain.model.GpaRecord) -> Unit
) {
    if (records.isEmpty()) {
        EmptyState("No GPA records yet.\nCalculate and save from the GPA Calculator.")
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(records, key = { it.id }) { record ->
            RecordCard(
                title = "GPA: ${record.gpa}",
                subtitle = "Credits: ${record.totalCredits} • Courses: ${record.courses.size}",
                timestamp = record.timestamp,
                onDelete = { onDelete(record.id) },
                onReload = { onReload(record) }
            )
        }
    }
}

@Composable
private fun CgpaRecordsList(
    records: List<me.jitish.gradevitian.domain.model.CgpaRecord>,
    onDelete: (String) -> Unit,
    onReload: (me.jitish.gradevitian.domain.model.CgpaRecord) -> Unit
) {
    if (records.isEmpty()) {
        EmptyState("No CGPA records yet.\nCalculate and save from the CGPA Calculator.")
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(records, key = { it.id }) { record ->
            RecordCard(
                title = "CGPA: ${record.cgpa}",
                subtitle = "Credits: ${record.totalCredits} • Semesters: ${record.semesters.size}",
                timestamp = record.timestamp,
                onDelete = { onDelete(record.id) },
                onReload = { onReload(record) }
            )
        }
    }
}

@Composable
private fun AttendanceRecordsList(
    records: List<me.jitish.gradevitian.domain.model.AttendanceRecord>,
    onDelete: (String) -> Unit
) {
    if (records.isEmpty()) {
        EmptyState("No attendance records yet.\nCalculate and save from the Attendance Calculator.")
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(records, key = { it.id }) { record ->
            RecordCard(
                title = "Attendance: ${record.attendancePercentage}%",
                subtitle = "Present: ${record.classesPresent} • Absent: ${record.classesAbsent} • Total: ${record.totalClasses}",
                timestamp = record.timestamp,
                onDelete = { onDelete(record.id) }
            )
        }
    }
}

@Composable
private fun RecordCard(
    title: String,
    subtitle: String,
    timestamp: Long,
    onDelete: () -> Unit,
    onReload: (() -> Unit)? = null
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateFormat.format(Date(timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (onReload != null) {
                IconButton(onClick = onReload) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.height(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

