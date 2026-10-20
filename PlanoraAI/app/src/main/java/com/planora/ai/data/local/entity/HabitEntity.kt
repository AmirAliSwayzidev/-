package com.planora.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val frequency: String = "DAILY", // DAILY, WEEKLY, CUSTOM
    val targetCount: Int = 1, // times per period
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val colorHex: String = "#22D3EE",
    val reminderTime: LocalTime? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // yyyy-MM-dd
    val completed: Boolean = true,
    val note: String = "",
    val loggedAt: LocalDateTime = LocalDateTime.now()
)
