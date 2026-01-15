package com.example.budgetingapp.domain.model

import java.time.LocalDate

/**
 * Represents a recurring expense template.
 *
 * Recurring expenses automatically generate expense entries based on their frequency.
 *
 * @param id Unique identifier (0 for new recurring expenses before saving)
 * @param amount Amount of the recurring expense
 * @param category The expense category
 * @param categoryCustomName Custom category name if category is OTHER
 * @param description Optional notes about the recurring expense
 * @param frequency How often this expense recurs
 * @param startDate When this recurring expense starts
 * @param nextDueDate Next date this expense should be generated
 * @param isActive If false, this recurring expense is paused and won't generate new expenses
 */
data class RecurringExpense(
    val id: Long = 0,
    val amount: Double,
    val category: ExpenseCategory,
    val categoryCustomName: String? = null,
    val description: String? = null,
    val frequency: RecurrenceFrequency,
    val startDate: LocalDate,
    val nextDueDate: LocalDate,
    val isActive: Boolean = true
) {
    /**
     * Get the display name for this expense's category.
     */
    fun getCategoryDisplayName(): String {
        return if (category == ExpenseCategory.OTHER && !categoryCustomName.isNullOrBlank()) {
            categoryCustomName
        } else {
            category.displayName
        }
    }

    /**
     * Calculate the next due date after the expense has been generated.
     */
    fun calculateNextDueDate(): LocalDate {
        return frequency.calculateNextDate(nextDueDate)
    }
}
