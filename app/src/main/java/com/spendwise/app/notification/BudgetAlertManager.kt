package com.spendwise.app.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.spendwise.app.domain.usecase.BudgetAlert
import java.text.NumberFormat
import java.util.Locale

class BudgetAlertManager(private val context: Context) {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }

    fun sendAlert(alert: BudgetAlert) {
        val title = if (alert.isOverBudget) {
            "${alert.categoryName} budget exceeded!"
        } else {
            "${alert.categoryName} budget at ${alert.percentage}%"
        }
        val text = "Spent ${currencyFormat.format(alert.spent)} of ${currencyFormat.format(alert.limit)}"

        val notification = NotificationCompat.Builder(context, NotificationChannelSetup.BUDGET_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                (2000 + alert.categoryName.hashCode()).and(0x7FFFFFFF),
                notification
            )
        } catch (_: SecurityException) { }
    }
}
