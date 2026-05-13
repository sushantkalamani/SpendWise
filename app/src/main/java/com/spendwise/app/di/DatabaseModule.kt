package com.spendwise.app.di

import com.spendwise.app.data.backup.DatabaseBackupManager
import com.spendwise.app.data.export.CsvExporter
import com.spendwise.app.data.export.CsvImporter
import com.spendwise.app.data.local.AppDatabase
import com.spendwise.app.data.local.UserPreferencesDataStore
import com.spendwise.app.data.local.dataStore
import com.spendwise.app.notification.BudgetAlertManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for database, data-store, and data-management singletons.
 */
val databaseModule = module {
    single { AppDatabase.create(androidContext()) }
    single { get<AppDatabase>().expenseDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().budgetDao() }
    single { UserPreferencesDataStore(androidContext().dataStore) }
    single { BudgetAlertManager(androidContext()) }

    // Data management utilities
    single { CsvExporter(androidContext()) }
    single { CsvImporter(androidContext()) }
    single { DatabaseBackupManager(androidContext(), get()) }
}
