package com.spendwise.app.domain.usecase

import com.spendwise.app.domain.model.MonthPeriod
import com.spendwise.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*

class GetMonthPeriodUseCase(
    private val prefsRepository: UserPreferencesRepository
) {
    suspend fun getCurrentPeriod(): MonthPeriod {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return getPeriodForDate(today)
    }

    suspend fun getPeriodForDate(date: LocalDate): MonthPeriod {
        val isCalendar = prefsRepository.isCalendarMode.first()
        return if (isCalendar) {
            getCalendarPeriod(date)
        } else {
            val salaryDay = prefsRepository.salaryDay.first()
            getSalaryPeriod(date, salaryDay)
        }
    }

    fun getCalendarPeriod(date: LocalDate): MonthPeriod {
        val start = LocalDate(date.year, date.month, 1)
        val end = start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        val label = "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}"
        return MonthPeriod(startDate = start, endDate = end, label = label)
    }

    fun getSalaryPeriod(date: LocalDate, salaryDay: Int): MonthPeriod {
        val clampedSalaryDay = salaryDay.coerceIn(1, 28)
        val dayOfMonth = date.dayOfMonth

        val periodStart: LocalDate
        val periodEnd: LocalDate

        if (dayOfMonth >= clampedSalaryDay) {
            periodStart = LocalDate(date.year, date.month, clampedSalaryDay)
            val nextMonth = periodStart.plus(1, DateTimeUnit.MONTH)
            periodEnd = LocalDate(nextMonth.year, nextMonth.month, clampedSalaryDay)
                .minus(1, DateTimeUnit.DAY)
        } else {
            val prevMonth = LocalDate(date.year, date.month, 1).minus(1, DateTimeUnit.MONTH)
            periodStart = LocalDate(prevMonth.year, prevMonth.month, clampedSalaryDay)
            periodEnd = LocalDate(date.year, date.month, clampedSalaryDay)
                .minus(1, DateTimeUnit.DAY)
        }

        val label = "${periodStart.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${periodStart.year}"
        return MonthPeriod(startDate = periodStart, endDate = periodEnd, label = label)
    }

    suspend fun getNextPeriod(currentPeriod: MonthPeriod): MonthPeriod {
        val nextDate = currentPeriod.endDate.plus(1, DateTimeUnit.DAY)
        return getPeriodForDate(nextDate)
    }

    suspend fun getPreviousPeriod(currentPeriod: MonthPeriod): MonthPeriod {
        val prevDate = currentPeriod.startDate.minus(1, DateTimeUnit.DAY)
        return getPeriodForDate(prevDate)
    }
}
