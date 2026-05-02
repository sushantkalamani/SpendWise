package com.spendwise.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spendwise.app.domain.model.Expense
import com.spendwise.app.domain.model.RecurringInterval
import com.spendwise.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RecurringExpenseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val expenseRepository: ExpenseRepository by inject()

    override suspend fun doWork(): Result {
        val recurring = expenseRepository.getRecurringExpenses()
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        for (expense in recurring) {
            val interval = expense.recurringInterval ?: continue
            val lastDate = expense.date.date
            val shouldCreate = when (interval) {
                RecurringInterval.DAILY -> true
                RecurringInterval.WEEKLY -> today.date.toEpochDays() - lastDate.toEpochDays() >= 7
                RecurringInterval.MONTHLY -> today.date.monthNumber != lastDate.monthNumber || today.date.year != lastDate.year
                RecurringInterval.YEARLY -> today.date.year != lastDate.year
            }

            if (shouldCreate) {
                val newExpense = Expense(
                    amount = expense.amount,
                    category = expense.category,
                    description = expense.description,
                    date = today,
                    paymentMethod = expense.paymentMethod,
                    tags = expense.tags,
                    source = expense.source,
                    isRecurring = false
                )
                expenseRepository.addExpense(newExpense)
            }
        }

        return Result.success()
    }
}
