package com.spendwise.app.domain.model

import kotlinx.datetime.LocalDate

data class MonthPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val label: String
)
