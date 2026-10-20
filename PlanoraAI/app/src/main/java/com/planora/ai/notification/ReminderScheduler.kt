package com.planora.ai.notification

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    fun scheduleDailyPlanning(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyPlanningWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_planning",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun scheduleTaskReminder(context: Context, taskId: Long, delayMinutes: Long) {
        val data = workDataOf("taskId" to taskId)
        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
