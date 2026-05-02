package com.spendwise.app.domain.model

data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val monthlyLimit: Double,
    val isOverallBudget: Boolean = false
)
