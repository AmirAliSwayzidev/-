package com.planora.ai.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.planora.ai.R
import com.planora.ai.data.local.PlanoraDatabase
import com.planora.ai.data.local.entity.TaskEntity
import com.planora.ai.data.repository.TaskRepository
import com.planora.ai.ui.components.OfflineBanner
import com.planora.ai.ui.components.TaskCard
import com.planora.ai.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    taskRepository: TaskRepository,
    database: PlanoraDatabase,
    onAddTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    onOpenStats: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val today = LocalDateTime.now()
    val tasks by taskRepository.getTasksForDate(today).collectAsState(initial = emptyList())
    val activeTasks by taskRepository.getActiveTasks().collectAsState(initial = emptyList())
    val prefs by database.preferencesDao().getPreferences().collectAsState(initial = null)

    val completedToday = tasks.count { it.status == 2 }
    val pendingToday = tasks.count { it.status != 2 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.today),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = today.format(
                                DateTimeFormatter.ofPattern("EEEE، d MMMM", Locale("fa"))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenStats) {
                        Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.statistics))
                    }
                    IconButton(onClick = onAddTask) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = VioletPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
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
                OfflineBanner()
            }

            item {
                // Summary cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = stringResource(R.string.completed),
                        value = completedToday.toString(),
                        color = Success,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = stringResource(R.string.pending),
                        value = pendingToday.toString(),
                        color = VioletPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = stringResource(R.string.overdue),
                        value = activeTasks.count {
                            it.dueDate != null && it.dueDate.isBefore(LocalDateTime.now()) && it.status != 2
                        }.toString(),
                        color = Error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.tasks),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.no_tasks),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onEditTask(task.id) },
                        onComplete = {
                            scope.launch { taskRepository.markCompleted(task.id) }
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
