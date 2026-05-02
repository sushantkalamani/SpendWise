package com.spendwise.app.di

import com.spendwise.app.ui.addexpense.AddExpenseViewModel
import com.spendwise.app.ui.analytics.AnalyticsViewModel
import com.spendwise.app.ui.history.HistoryViewModel
import com.spendwise.app.ui.home.HomeViewModel
import com.spendwise.app.ui.categories.CategoriesViewModel
import com.spendwise.app.ui.onboarding.OnboardingViewModel
import com.spendwise.app.ui.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { AddExpenseViewModel(get(), get()) }
    viewModel { OnboardingViewModel(get(), get(), get()) }
    viewModel { AnalyticsViewModel(get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get(), get()) }
    viewModel { CategoriesViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
}
