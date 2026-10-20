package com.planora.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val note: String = "",
    val priority: Int = 1,
    val status: Int = 0,
    val dueDate: LocalDateTime? = null,
    val scheduledStart: LocalDateTime? = null,
    val scheduledEnd: LocalDateTime? = null,
    val estimatedMinutes: Int = 30,
    val actualMinutes: Int? = null,
    val goalId: Long? = null,
    val projectId: Long? = null,
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null,
    val reminderMinutesBefore: Int? = 15,
    val tags: String = "",
    val colorHex: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
)
