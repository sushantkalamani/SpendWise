package com.spendwise.app

import android.app.Application
import com.spendwise.app.di.appModule
import com.spendwise.app.di.databaseModule
import com.spendwise.app.di.domainModule
import com.spendwise.app.notification.NotificationChannelSetup
import com.spendwise.app.notification.ReminderScheduler
import com.spendwise.app.worker.RecurringExpenseWorker
import androidx.work.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class SpendWiseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SpendWiseApp)
            modules(databaseModule, domainModule, appModule)
        }
        NotificationChannelSetup.createChannels(this)
        ReminderScheduler.schedule(this, 21, 0)
        scheduleRecurringExpenseWorker()
    }

    private fun scheduleRecurringExpenseWorker() {
        val request = PeriodicWorkRequestBuilder<RecurringExpenseWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_expenses",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
