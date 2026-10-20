package com.planora.ai.ui.screens.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.planora.ai.R
import com.planora.ai.data.local.PlanoraDatabase
import com.planora.ai.data.local.entity.HabitEntity
import com.planora.ai.data.local.entity.HabitLogEntity
import com.planora.ai.ui.theme.CyanAccent
import com.planora.ai.ui.theme.Success
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(database: PlanoraDatabase, onBack: () -> Unit) {
    val habits by database.habitDao().getActiveHabits().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    val today = LocalDate.now().toString()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.habits)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = CyanAccent) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        if (habits.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.empty_habits), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habits) { habit ->
                    var completedToday by remember { mutableStateOf(false) }
                    LaunchedEffect(habit.id) {
                        completedToday = database.habitDao().getLogForDate(habit.id, today) != null
                    }
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(habit.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${stringResource(R.string.streak)}: ${habit.currentStreak}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Checkbox(
                                checked = completedToday,
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        if (checked) {
                                            database.habitDao().insertLog(
                                                HabitLogEntity(habitId = habit.id, date = today)
                                            )
                                            database.habitDao().update(
                                                habit.copy(currentStreak = habit.currentStreak + 1)
                                            )
                                        } else {
                                            database.habitDao().deleteLog(habit.id, today)
                                        }
                                        completedToday = checked
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Success)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.habits)) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text(stringResource(R.string.title)) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank()) {
                        scope.launch {
                            database.habitDao().insert(
                                HabitEntity(title = newTitle.trim(), createdAt = LocalDateTime.now())
                            )
                            newTitle = ""
                            showDialog = false
                        }
                    }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
