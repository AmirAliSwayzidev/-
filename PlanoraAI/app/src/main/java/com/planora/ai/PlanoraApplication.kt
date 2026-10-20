package com.planora.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.planora.ai.data.local.PlanoraDatabase
import com.planora.ai.data.repository.TaskRepository
import com.planora.ai.domain.planner.OfflinePlanningEngine
import com.planora.ai.notification.ReminderScheduler
import com.planora.ai.session.SessionTimerManager

class PlanoraApplication : Application() {

    lateinit var database: PlanoraDatabase
        private set

    lateinit var taskRepository: TaskRepository
        private set

    val offlinePlanningEngine = OfflinePlanningEngine()

    override fun onCreate() {
        super.onCreate()
        database = PlanoraDatabase.getInstance(this)
        taskRepository = TaskRepository(database.taskDao())
        createNotificationChannels()
        ReminderScheduler.scheduleDailyPlanning(this)
        SessionTimerManager.init(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val taskChannel = NotificationChannel(
                CHANNEL_TASKS,
                "یادآوری کارها",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان‌های مربوط به کارها و مهلت‌ها"
            }

            val habitChannel = NotificationChannel(
                CHANNEL_HABITS,
                "یادآوری عادت‌ها",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "اعلان‌های مربوط به عادت‌های روزانه"
            }

            val planningChannel = NotificationChannel(
                CHANNEL_PLANNING,
                "برنامه‌ریزی روزانه",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "یادآوری برنامه‌ریزی روزانه"
            }

            val sessionChannel = NotificationChannel(
                "planora_session",
                "جلسات تمرین",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "پایان تایمر جلسه تمرین" }
            manager.createNotificationChannels(listOf(taskChannel, habitChannel, planningChannel, sessionChannel))
        }
    }

    companion object {
        const val CHANNEL_TASKS = "tasks_channel"
        const val CHANNEL_HABITS = "habits_channel"
        const val CHANNEL_PLANNING = "planning_channel"
    }
}
