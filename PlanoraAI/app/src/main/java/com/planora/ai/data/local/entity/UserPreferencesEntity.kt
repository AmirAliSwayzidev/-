package com.planora.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * API key is stored ONLY on device in Room/DataStore.
 * Never ship a default secret in the APK.
 *
 * Where to set API key in the app:
 * Profile / Settings → فیلدهای apiBaseUrl, apiKey, apiModel
 * Or programmatically via PreferencesDao.
 */
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 1,
    val displayName: String = "",
    val preferredLanguage: String = "fa",
    val themeMode: String = "system",
    val preferredStartHour: Int = 8,
    val preferredEndHour: Int = 22,
    val dailyWorkloadLimitHours: Float = 8f,
    val peakEnergyTime: String = "morning",
    val breakDurationMinutes: Int = 15,
    val enableNotifications: Boolean = true,
    val enableAiSuggestions: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val hasAcceptedAiPrivacy: Boolean = false,
    // ── User-provided AI config (local only) ──
    val apiBaseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val apiModel: String = "gpt-4o-mini",
    val apiProvider: String = "openai"
)
