package com.planora.ai.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.planora.ai.MainActivity
import com.planora.ai.PlanoraApplication
import com.planora.ai.R

class DailyPlanningWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, PlanoraApplication.CHANNEL_PLANNING)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle(applicationContext.getString(R.string.daily_planning))
            .setContentText("وقت برنامه‌ریزی روزانه است!")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)
        return Result.success()
    }
}

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val taskId = inputData.getLong("taskId", -1)
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            applicationContext, taskId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, PlanoraApplication.CHANNEL_TASKS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(applicationContext.getString(R.string.task_reminder))
            .setContentText("یادآوری کار #$taskId")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(taskId.toInt(), notification)
        return Result.success()
    }
}
