package com.spendwise.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannelSetup {
    const val REMINDER_CHANNEL_ID = "daily_reminder"
    const val BUDGET_CHANNEL_ID = "budget_alerts"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Daily Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Daily reminder to log your expenses" }

            val budgetChannel = NotificationChannel(
                BUDGET_CHANNEL_ID,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Alerts when you approach or exceed your budget" }

            manager.createNotificationChannel(reminderChannel)
            manager.createNotificationChannel(budgetChannel)
        }
    }
}
