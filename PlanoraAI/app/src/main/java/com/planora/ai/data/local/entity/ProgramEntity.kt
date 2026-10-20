package com.planora.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Multi-day training / study program (separate from daily tasks). */
@Entity(tableName = "programs")
data class ProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String = "sport", // sport | study | custom
    val description: String = "",
    val color: String = "#F97316",
    val days: Int = 7,
    val startDate: String = "", // yyyy-MM-dd
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "program_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("programId")]
)
data class ProgramSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val dayIndex: Int,
    val title: String,
    val durationMin: Int = 45,
    val timeOfDay: String = "18:00",
    val done: Boolean = false,
    val doneAt: Long? = null
)
