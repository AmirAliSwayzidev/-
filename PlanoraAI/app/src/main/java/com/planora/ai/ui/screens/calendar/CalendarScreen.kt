package com.planora.ai.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.planora.ai.R
import com.planora.ai.data.repository.TaskRepository
import com.planora.ai.ui.components.TaskCard
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    taskRepository: TaskRepository,
    onTaskClick: (Long) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val start = LocalDateTime.of(selectedDate, LocalTime.MIN)
    val tasks by taskRepository.getTasksForDate(start).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.calendar)) })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Simple date selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { selectedDate = selectedDate.minusDays(1) }) {
                    Text("◀")
                }
                Text(
                    text = selectedDate.toString(),
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(onClick = { selectedDate = selectedDate.plusDays(1) }) {
                    Text("▶")
                }
            }
            Spacer(Modifier.height(16.dp))
            if (tasks.isEmpty()) {
                Text(
                    stringResource(R.string.no_tasks),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tasks) { task ->
                        TaskCard(
                            task = task,
                            onClick = { onTaskClick(task.id) },
                            onComplete = {}
                        )
                    }
                }
            }
        }
    }
}
