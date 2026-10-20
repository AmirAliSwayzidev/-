package com.planora.ai.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.planora.ai.R
import com.planora.ai.data.local.PlanoraDatabase
import com.planora.ai.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    database: PlanoraDatabase,
    onOpenHabits: () -> Unit,
    onOpenStats: () -> Unit
) {
    val prefs by database.preferencesDao().getPreferences().collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SettingsItem(Icons.Default.Loop, stringResource(R.string.habits), onOpenHabits)
            SettingsItem(Icons.Default.BarChart, stringResource(R.string.statistics), onOpenStats)

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleSmall)
            listOf("system", "dark", "light").forEach { mode ->
                val label = when (mode) {
                    "dark" -> stringResource(R.string.dark_mode)
                    "light" -> stringResource(R.string.light_mode)
                    else -> stringResource(R.string.system_default)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                val current = prefs ?: UserPreferencesEntity()
                                database.preferencesDao().insert(current.copy(themeMode = mode))
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = (prefs?.themeMode ?: "system") == mode, onClick = null)
                    Text(label, modifier = Modifier.padding(start = 8.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(stringResource(R.string.language), style = MaterialTheme.typography.titleSmall)
            listOf("fa", "en").forEach { lang ->
                val label = if (lang == "fa") stringResource(R.string.persian) else stringResource(R.string.english)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                val current = prefs ?: UserPreferencesEntity()
                                database.preferencesDao().insert(current.copy(preferredLanguage = lang))
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = (prefs?.preferredLanguage ?: "fa") == lang, onClick = null)
                    Text(label, modifier = Modifier.padding(start = 8.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(stringResource(R.string.privacy), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(R.string.ai_privacy_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun SettingsItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
