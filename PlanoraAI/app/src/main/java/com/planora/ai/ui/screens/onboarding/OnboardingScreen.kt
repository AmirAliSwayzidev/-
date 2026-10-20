package com.planora.ai.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.planora.ai.R
import com.planora.ai.data.local.PlanoraDatabase
import com.planora.ai.data.local.entity.UserPreferencesEntity
import com.planora.ai.ui.theme.VioletPrimary
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    database: PlanoraDatabase,
    onFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableIntStateOf(0) }
    var language by remember { mutableStateOf("fa") }
    var themeMode by remember { mutableStateOf("system") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (step) {
            0 -> {
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = VioletPrimary
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.onboarding_welcome),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.onboarding_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { step = 1 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text(stringResource(R.string.get_started), style = MaterialTheme.typography.titleMedium)
                }
            }
            1 -> {
                Text(
                    text = stringResource(R.string.select_language),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
                LanguageOption("فارسی", "fa", language) { language = it }
                LanguageOption("English", "en", language) { language = it }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { step = 2 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
            2 -> {
                Text(
                    text = stringResource(R.string.theme),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
                ThemeOption(stringResource(R.string.system_default), "system", themeMode) { themeMode = it }
                ThemeOption(stringResource(R.string.dark_mode), "dark", themeMode) { themeMode = it }
                ThemeOption(stringResource(R.string.light_mode), "light", themeMode) { themeMode = it }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        scope.launch {
                            database.preferencesDao().insert(
                                UserPreferencesEntity(
                                    preferredLanguage = language,
                                    themeMode = themeMode,
                                    hasCompletedOnboarding = true
                                )
                            )
                            onFinished()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text(stringResource(R.string.get_started))
                }
            }
        }
    }
}

@Composable
private fun LanguageOption(label: String, value: String, selected: String, onSelect: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .selectable(selected = selected == value, onClick = { onSelect(value) }),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected == value)
                VioletPrimary.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected == value, onClick = { onSelect(value) })
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ThemeOption(label: String, value: String, selected: String, onSelect: (String) -> Unit) {
    LanguageOption(label, value, selected, onSelect)
}
