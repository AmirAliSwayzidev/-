package com.planora.ai.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.planora.ai.R
import com.planora.ai.data.local.entity.TaskEntity
import com.planora.ai.data.repository.TaskRepository
import com.planora.ai.ui.theme.VioletPrimary
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    taskId: Long?,
    taskRepository: TaskRepository,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableIntStateOf(1) }
    var duration by remember { mutableIntStateOf(30) }
    var existing by remember { mutableStateOf<TaskEntity?>(null) }

    LaunchedEffect(taskId) {
        if (taskId != null) {
            val task = taskRepository.getTaskById(taskId)
            existing = task
            if (task != null) {
                title = task.title
                description = task.description
                priority = task.priority
                duration = task.estimatedMinutes
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (taskId == null) stringResource(R.string.create_task)
                        else stringResource(R.string.edit_task)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (taskId != null) {
                        IconButton(onClick = {
                            scope.launch {
                                existing?.let { taskRepository.delete(it) }
                                onDone()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Text(stringResource(R.string.priority), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    0 to stringResource(R.string.low),
                    1 to stringResource(R.string.medium),
                    2 to stringResource(R.string.high)
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = priority == value,
                        onClick = { priority = value },
                        label = { Text(label) }
                    )
                }
            }

            OutlinedTextField(
                value = duration.toString(),
                onValueChange = { duration = it.toIntOrNull() ?: 30 },
                label = { Text(stringResource(R.string.duration)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    scope.launch {
                        val now = LocalDateTime.now()
                        if (existing != null) {
                            taskRepository.update(
                                existing!!.copy(
                                    title = title.trim(),
                                    description = description.trim(),
                                    priority = priority,
                                    estimatedMinutes = duration,
                                    updatedAt = now
                                )
                            )
                        } else {
                            taskRepository.insert(
                                TaskEntity(
                                    title = title.trim(),
                                    description = description.trim(),
                                    priority = priority,
                                    estimatedMinutes = duration,
                                    dueDate = now.plusHours(2),
                                    scheduledStart = now.plusHours(1),
                                    scheduledEnd = now.plusHours(1).plusMinutes(duration.toLong())
                                )
                            )
                        }
                        onDone()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
