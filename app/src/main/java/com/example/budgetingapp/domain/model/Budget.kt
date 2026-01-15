package com.example.budgetingapp.domain.model

import java.time.LocalDate

/**
 * Represents the monthly budget settings.
 *
 * There should typically only be one active budget at a time.
 *
 * @param id Unique identifier (0 for new budget before saving)
 * @param monthlyAmount Total amount budgeted per month
 * @param startDate When this budget period starts (typically the 1st of the month)
 */
data class Budget(
    val id: Long = 0,
    val monthlyAmount: Double,
    val startDate: LocalDate
) {
    /**
     * Calculate the weekly budget amount.
     * Divides monthly amount by approximately 4.3 weeks per month (30 days / 7 days).
     */
    fun calculateWeeklyBudget(monthlyRecurringExpenses: Double): Double {
        val availableAmount = monthlyAmount - monthlyRecurringExpenses
        return availableAmount / 4.3 // Approximately 4.3 weeks in a month
    }
}
