package com.spendwise.app.di

import com.spendwise.app.data.repository.*
import com.spendwise.app.domain.repository.*
import com.spendwise.app.domain.usecase.*
import org.koin.dsl.module

val domainModule = module {
    single<ExpenseRepository> { ExpenseRepositoryImpl(get(), get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get(), get()) }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(get()) }

    factory { GetMonthPeriodUseCase(get()) }
    factory { GetMonthlySummaryUseCase(get(), get(), get()) }
    factory { GetCategoryBreakdownUseCase(get(), get(), get()) }
    factory { CheckBudgetAlertUseCase(get(), get()) }
    factory { GetMonthComparisonUseCase(get(), get()) }
}
