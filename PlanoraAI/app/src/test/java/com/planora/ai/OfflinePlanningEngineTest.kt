package com.planora.ai

import com.planora.ai.data.local.entity.TaskEntity
import com.planora.ai.data.local.entity.UserPreferencesEntity
import com.planora.ai.domain.planner.OfflinePlanningEngine
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class OfflinePlanningEngineTest {

    private val engine = OfflinePlanningEngine()
    private val prefs = UserPreferencesEntity(
        preferredStartHour = 9,
        preferredEndHour = 18,
        dailyWorkloadLimitHours = 6f,
        peakEnergyTime = "morning",
        breakDurationMinutes = 15
    )

    @Test
    fun `generateSchedule produces non-overlapping tasks`() {
        val tasks = listOf(
            TaskEntity(title = "Task A", priority = 2, estimatedMinutes = 60),
            TaskEntity(title = "Task B", priority = 1, estimatedMinutes = 45),
            TaskEntity(title = "Task C", priority = 0, estimatedMinutes = 30)
        )
        val result = engine.generateSchedule(tasks, prefs, days = 3)
        assertTrue(result.tasks.isNotEmpty())
        assertTrue(result.totalMinutes > 0)
        // Check no overlap
        val sorted = result.tasks.sortedBy { it.suggestedStart }
        for (i in 0 until sorted.size - 1) {
            assertTrue(
                "${sorted[i].title} overlaps ${sorted[i + 1].title}",
                !sorted[i].suggestedEnd.isAfter(sorted[i + 1].suggestedStart)
            )
        }
    }

    @Test
    fun `generateStudyPlan creates sessions`() {
        val result = engine.generateStudyPlan(
            examInDays = 5,
            subjects = listOf("Math", "Physics"),
            hoursPerDay = 2f,
            preferences = prefs
        )
        assertTrue(result.tasks.isNotEmpty())
        assertEquals(5 * 2, result.tasks.size) // approx sessions
    }

    @Test
    fun `high priority tasks scheduled first`() {
        val tasks = listOf(
            TaskEntity(title = "Low", priority = 0, estimatedMinutes = 30),
            TaskEntity(title = "High", priority = 2, estimatedMinutes = 30)
        )
        val result = engine.generateSchedule(tasks, prefs, days = 1)
        assertTrue(result.tasks.isNotEmpty())
        assertEquals("High", result.tasks.first().title)
    }
}
