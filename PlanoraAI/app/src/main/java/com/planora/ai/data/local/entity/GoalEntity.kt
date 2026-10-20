package com.planora.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "personal", // personal, study, work, health
    val targetDate: LocalDate? = null,
    val progress: Float = 0f, // 0.0 to 1.0
    val status: Int = 0, // 0=Active, 1=Completed, 2=Archived
    val colorHex: String = "#7C5CFC",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
