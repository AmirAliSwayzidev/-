package com.planora.ai.ui.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.planora.ai.R
import com.planora.ai.data.repository.TaskRepository
import com.planora.ai.ui.theme.Success
import com.planora.ai.ui.theme.VioletPrimary
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(taskRepository: TaskRepository, onBack: () -> Unit) {
    var completed by remember { mutableIntStateOf(0) }
    var pending by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        completed = taskRepository.countCompletedOn(LocalDateTime.now())
        pending = taskRepository.countPending()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistics)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(stringResource(R.string.productivity), style = MaterialTheme.typography.headlineMedium)
            StatCard(stringResource(R.string.completed), completed.toString(), Success)
            StatCard(stringResource(R.string.pending), pending.toString(), VioletPrimary)
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.displayMedium, color = color)
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
