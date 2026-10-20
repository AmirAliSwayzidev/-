package com.planora.ai.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.planora.ai.R
import com.planora.ai.data.local.PlanoraDatabase
import com.planora.ai.data.local.entity.UserPreferencesEntity
import com.planora.ai.data.remote.MultiProviderAiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    database: PlanoraDatabase,
    onOpenHabits: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenPrograms: () -> Unit = {}
) {
    val prefs by database.preferencesDao().getPreferences().collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var provider by remember(prefs) { mutableStateOf(prefs?.apiProvider ?: "openai") }
    var baseUrl by remember(prefs) { mutableStateOf(prefs?.apiBaseUrl ?: "https://api.openai.com/v1") }
    var apiKey by remember(prefs) { mutableStateOf(prefs?.apiKey ?: "") }
    var model by remember(prefs) { mutableStateOf(prefs?.apiModel ?: "gpt-4o-mini") }
    var showKey by remember { mutableStateOf(false) }
    var saveMsg by remember { mutableStateOf<String?>(null) }
    var providerMenu by remember { mutableStateOf(false) }

    val providers = listOf(
        "openai" to "OpenAI (GPT)",
        "grok" to "xAI Grok",
        "gemini" to "Google Gemini",
        "deepseek" to "DeepSeek",
        "groq" to "Groq (رایگان تست)",
        "custom" to "سفارشی"
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsItem(Icons.Default.FitnessCenter, "برنامه‌های تمرین / مطالعه", onOpenPrograms)
            SettingsItem(Icons.Default.Loop, stringResource(R.string.habits), onOpenHabits)
            SettingsItem(Icons.Default.BarChart, stringResource(R.string.statistics), onOpenStats)

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // ══════ API KEY SECTION ══════
            Text("اتصال هوش مصنوعی", style = MaterialTheme.typography.titleMedium)
            Text(
                "کلید فقط روی این گوشی ذخیره می‌شود. GPT / Grok / Gemini / DeepSeek / Groq",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ExposedDropdownMenuBox(
                expanded = providerMenu,
                onExpandedChange = { providerMenu = it }
            ) {
                OutlinedTextField(
                    value = providers.find { it.first == provider }?.second ?: provider,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ارائه‌دهنده") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(providerMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(expanded = providerMenu, onDismissRequest = { providerMenu = false }) {
                    providers.forEach { (id, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                provider = id
                                MultiProviderAiClient.presets[id]?.let { p ->
                                    baseUrl = p.baseUrl
                                    model = p.model
                                }
                                providerMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Base URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("مدل") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text("فقط خود کلید انگلیسی، بدون فاصله یا فارسی")
                }
            )

            Button(
                onClick = {
                    scope.launch {
                        val current = prefs ?: UserPreferencesEntity()
                        val clean = MultiProviderAiClient.sanitizeKey(apiKey)
                        database.preferencesDao().insert(
                            current.copy(
                                apiProvider = provider,
                                apiBaseUrl = baseUrl.trim(),
                                apiModel = model.trim(),
                                apiKey = clean
                            )
                        )
                        apiKey = clean
                        saveMsg = if (clean.length >= 8) "ذخیره شد ✓" else "کلید خیلی کوتاه است"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ذخیره تنظیمات API")
            }
            saveMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            if (!prefs?.apiKey.isNullOrBlank()) {
                Text(
                    "کلید فعلی: ${prefs!!.apiKey.take(6)}…${prefs!!.apiKey.takeLast(4)}  |  ${prefs!!.apiProvider}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleSmall)
            listOf("system", "dark", "light").forEach { mode ->
                val label = when (mode) {
                    "dark" -> stringResource(R.string.dark_mode)
                    "light" -> stringResource(R.string.light_mode)
                    else -> stringResource(R.string.system_default)
                }
                Row(
                    Modifier
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
                    Text(label, Modifier.padding(start = 8.dp))
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(stringResource(R.string.language), style = MaterialTheme.typography.titleSmall)
            listOf("fa", "en").forEach { lang ->
                val label = if (lang == "fa") stringResource(R.string.persian) else stringResource(R.string.english)
                Row(
                    Modifier
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
                    Text(label, Modifier.padding(start = 8.dp))
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(stringResource(R.string.privacy), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(R.string.ai_privacy_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
