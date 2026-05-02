package com.spendwise.app.di

import com.spendwise.app.data.local.AppDatabase
import com.spendwise.app.data.local.UserPreferencesDataStore
import com.spendwise.app.data.local.dataStore
import com.spendwise.app.notification.BudgetAlertManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.create(androidContext()) }
    single { get<AppDatabase>().expenseDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().budgetDao() }
    single { UserPreferencesDataStore(androidContext().dataStore) }
    single { BudgetAlertManager(androidContext()) }
}
