package com.spendwise.app.domain.model

import kotlinx.datetime.LocalDateTime

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val category: Category?,
    val description: String = "",
    val date: LocalDateTime,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val tags: List<String> = emptyList(),
    val upiRefId: String? = null,
    val merchantVpa: String? = null,
    val source: ExpenseSource = ExpenseSource.MANUAL,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval? = null
)

enum class PaymentMethod { UPI, CASH, CARD, NET_BANKING, OTHER }
enum class ExpenseSource { MANUAL, SMS, NOTIFICATION, IMPORT }
enum class RecurringInterval { DAILY, WEEKLY, MONTHLY, YEARLY }
