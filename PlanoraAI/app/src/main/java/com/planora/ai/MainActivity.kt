package com.planora.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.planora.ai.ui.navigation.PlanoraNavGraph
import com.planora.ai.ui.theme.PlanoraTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as PlanoraApplication
        val prefsFlow = app.database.preferencesDao().getPreferences()

        setContent {
            val prefs by prefsFlow.collectAsState(initial = null)
            val isDark = when (prefs?.themeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            val isRtl = (prefs?.preferredLanguage ?: "fa") == "fa"

            CompositionLocalProvider(
                LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                PlanoraTheme(darkTheme = isDark) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        PlanoraNavGraph(
                            database = app.database,
                            offlineEngine = app.offlinePlanningEngine,
                            taskRepository = app.taskRepository
                        )
                    }
                }
            }
        }
    }
}
