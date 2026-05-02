package com.spendwise.app.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spendwise.app.R
import com.spendwise.app.data.local.dao.ExpenseDao
import com.spendwise.app.notification.NotificationChannelSetup
import com.spendwise.app.ui.MainActivity
import kotlinx.datetime.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val expenseDao: ExpenseDao by inject()

    override suspend fun doWork(): Result {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startOfDay = today.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endOfDay = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds() - 1

        // Count today's expenses using a direct query
        val todayExpenses = expenseDao.getCountForDateRange(startOfDay, endOfDay)

        val message = if (todayExpenses > 0) {
            "You logged $todayExpenses expense${if (todayExpenses > 1) "s" else ""} today. Tap to add more."
        } else {
            "You haven't logged any expenses today. Tap to add."
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannelSetup.REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("SpendWise Reminder")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(1001, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted
        }

        return Result.success()
    }
}
