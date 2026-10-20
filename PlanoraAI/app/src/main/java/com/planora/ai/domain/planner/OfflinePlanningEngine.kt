package com.planora.ai.domain.planner

import com.planora.ai.data.local.entity.TaskEntity
import com.planora.ai.data.local.entity.UserPreferencesEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.min

/**
 * Rule-based offline planning engine.
 * Generates realistic schedules based on user preferences, deadlines, priorities,
 * and available time slots without requiring network access.
 */
class OfflinePlanningEngine {

    data class TimeSlot(
        val start: LocalDateTime,
        val end: LocalDateTime
    ) {
        val durationMinutes: Long
            get() = ChronoUnit.MINUTES.between(start, end)
    }

    data class SuggestedTask(
        val title: String,
        val description: String = "",
        val priority: Int,
        val estimatedMinutes: Int,
        val suggestedStart: LocalDateTime,
        val suggestedEnd: LocalDateTime,
        val reason: String
    )

    data class PlanResult(
        val tasks: List<SuggestedTask>,
        val warnings: List<String>,
        val explanation: String,
        val totalMinutes: Int
    )

    /**
     * Generate a basic schedule for a list of pending tasks within a date range.
     */
    fun generateSchedule(
        pendingTasks: List<TaskEntity>,
        preferences: UserPreferencesEntity,
        startDate: LocalDate = LocalDate.now(),
        days: Int = 7,
        existingScheduled: List<TaskEntity> = emptyList()
    ): PlanResult {
        val warnings = mutableListOf<String>()
        val suggested = mutableListOf<SuggestedTask>()
        var totalMinutes = 0

        val dailyLimitMinutes = (preferences.dailyWorkloadLimitHours * 60).toInt()
        val workStart = LocalTime.of(preferences.preferredStartHour.coerceIn(5, 12), 0)
        val workEnd = LocalTime.of(preferences.preferredEndHour.coerceIn(16, 23), 0)
        val breakMinutes = preferences.breakDurationMinutes.coerceIn(5, 60)

        // Sort tasks: high priority + earlier deadline first
        val sorted = pendingTasks
            .filter { it.status != 2 }
            .sortedWith(
                compareByDescending<TaskEntity> { it.priority }
                    .thenBy { it.dueDate ?: LocalDateTime.MAX }
            )

        val occupiedSlots = existingScheduled
            .filter { it.scheduledStart != null && it.scheduledEnd != null }
            .map { TimeSlot(it.scheduledStart!!, it.scheduledEnd!!) }
            .toMutableList()

        var currentDate = startDate
        val endDate = startDate.plusDays(days.toLong())

        for (task in sorted) {
            if (currentDate.isAfter(endDate)) {
                warnings.add("Could not schedule all tasks within $days days.")
                break
            }

            val duration = task.estimatedMinutes.coerceIn(15, 240)
            var placed = false
            var dayCursor = currentDate

            while (!dayCursor.isAfter(endDate) && !placed) {
                val daySlots = findFreeSlots(
                    date = dayCursor,
                    workStart = workStart,
                    workEnd = workEnd,
                    occupied = occupiedSlots,
                    minDuration = duration,
                    breakMinutes = breakMinutes
                )

                val slot = daySlots.firstOrNull { it.durationMinutes >= duration }
                if (slot != null) {
                    val start = slot.start
                    val end = start.plusMinutes(duration.toLong())
                    suggested.add(
                        SuggestedTask(
                            title = task.title,
                            description = task.description,
                            priority = task.priority,
                            estimatedMinutes = duration,
                            suggestedStart = start,
                            suggestedEnd = end,
                            reason = buildReason(task, preferences)
                        )
                    )
                    occupiedSlots.add(TimeSlot(start, end.plusMinutes(breakMinutes.toLong())))
                    totalMinutes += duration
                    placed = true
                } else {
                    dayCursor = dayCursor.plusDays(1)
                }
            }

            if (!placed) {
                warnings.add("Could not find a free slot for: ${task.title}")
            }
        }

        // Check for unrealistic load
        val avgDaily = if (days > 0) totalMinutes / days else 0
        if (avgDaily > dailyLimitMinutes) {
            warnings.add(
                "Average daily load (${avgDaily} min) exceeds your preferred limit " +
                        "(${dailyLimitMinutes} min). Consider extending the period or reducing tasks."
            )
        }

        val explanation = buildString {
            append("Generated a ${days}-day schedule with ${suggested.size} tasks. ")
            append("Working hours: ${workStart} – ${workEnd}. ")
            append("Peak energy preference: ${preferences.peakEnergyTime}. ")
            if (warnings.isEmpty()) append("No major conflicts detected.")
        }

        return PlanResult(
            tasks = suggested,
            warnings = warnings,
            explanation = explanation,
            totalMinutes = totalMinutes
        )
    }

    /**
     * Simple study plan generator from natural parameters (offline fallback).
     */
    fun generateStudyPlan(
        examInDays: Int,
        subjects: List<String>,
        hoursPerDay: Float,
        preferences: UserPreferencesEntity
    ): PlanResult {
        val dailyMinutes = (hoursPerDay * 60).toInt().coerceIn(30, 360)
        val sessionMinutes = 45
        val sessionsPerDay = (dailyMinutes / (sessionMinutes + preferences.breakDurationMinutes))
            .coerceAtLeast(1)

        val suggested = mutableListOf<SuggestedTask>()
        var total = 0
        val start = LocalDate.now()

        for (dayOffset in 0 until examInDays) {
            val date = start.plusDays(dayOffset.toLong())
            val subject = subjects[dayOffset % subjects.size]
            val peakHour = when (preferences.peakEnergyTime) {
                "afternoon" -> 14
                "evening" -> 18
                else -> 9
            }

            var hour = peakHour
            repeat(sessionsPerDay) {
                val startDt = LocalDateTime.of(date, LocalTime.of(hour.coerceIn(7, 21), 0))
                val endDt = startDt.plusMinutes(sessionMinutes.toLong())
                suggested.add(
                    SuggestedTask(
                        title = "مطالعه: $subject",
                        description = "جلسه مطالعه برای $subject (آفلاین)",
                        priority = if (dayOffset >= examInDays - 3) 2 else 1,
                        estimatedMinutes = sessionMinutes,
                        suggestedStart = startDt,
                        suggestedEnd = endDt,
                        reason = "جلسه مطالعه بر اساس ترجیحات انرژی و محدودیت روزانه"
                    )
                )
                total += sessionMinutes
                hour += 1
            }
        }

        return PlanResult(
            tasks = suggested,
            warnings = if (examInDays < 3) listOf("زمان بسیار محدود است؛ اولویت‌بندی شدید توصیه می‌شود.") else emptyList(),
            explanation = "برنامه مطالعه $examInDays روزه برای ${subjects.size} موضوع با حدود $hoursPerDay ساعت در روز تولید شد.",
            totalMinutes = total
        )
    }

    private fun findFreeSlots(
        date: LocalDate,
        workStart: LocalTime,
        workEnd: LocalTime,
        occupied: List<TimeSlot>,
        minDuration: Int,
        breakMinutes: Int
    ): List<TimeSlot> {
        val dayStart = LocalDateTime.of(date, workStart)
        val dayEnd = LocalDateTime.of(date, workEnd)
        val dayOccupied = occupied
            .filter { it.start.toLocalDate() == date }
            .sortedBy { it.start }

        val free = mutableListOf<TimeSlot>()
        var cursor = dayStart

        for (occ in dayOccupied) {
            if (occ.start.isAfter(cursor)) {
                val gap = TimeSlot(cursor, occ.start)
                if (gap.durationMinutes >= minDuration) free.add(gap)
            }
            if (occ.end.isAfter(cursor)) cursor = occ.end
        }
        if (cursor.isBefore(dayEnd)) {
            val gap = TimeSlot(cursor, dayEnd)
            if (gap.durationMinutes >= minDuration) free.add(gap)
        }
        return free
    }

    private fun buildReason(task: TaskEntity, prefs: UserPreferencesEntity): String {
        val priorityLabel = when (task.priority) {
            2 -> "اولویت بالا"
            1 -> "اولویت متوسط"
            else -> "اولویت پایین"
        }
        return "$priorityLabel • مدت تخمینی ${task.estimatedMinutes} دقیقه • بر اساس ساعات کاری ترجیحی"
    }
}
