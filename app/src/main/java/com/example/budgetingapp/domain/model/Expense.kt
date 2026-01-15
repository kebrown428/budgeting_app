package com.example.budgetingapp.domain.model

import java.time.LocalDateTime

/**
 * Represents a single expense entry.
 *
 * @param id Unique identifier (0 for new expenses before saving)
 * @param amount Amount spent
 * @param category The expense category
 * @param categoryCustomName Custom category name if category is OTHER
 * @param date Date and time when the expense occurred
 * @param description Optional notes about the expense
 * @param isFromSlushFund If true, this expense was paid from the slush fund instead of weekly budget
 * @param isRecurring If true, this expense was auto-generated from a recurring expense
 * @param recurringExpenseId Reference to the recurring expense that generated this (if isRecurring is true)
 */
data class Expense(
    val id: Long = 0,
    val amount: Double,
    val category: ExpenseCategory,
    val categoryCustomName: String? = null,
    val date: LocalDateTime,
    val description: String? = null,
    val isFromSlushFund: Boolean = false,
    val isRecurring: Boolean = false,
    val recurringExpenseId: Long? = null
) {
    /**
     * Get the display name for this expense's category.
     * Returns the custom name if provided, otherwise the category's display name.
     */
    fun getCategoryDisplayName(): String {
        return if (category == ExpenseCategory.OTHER && !categoryCustomName.isNullOrBlank()) {
            categoryCustomName
        } else {
            category.displayName
        }
    }
}
