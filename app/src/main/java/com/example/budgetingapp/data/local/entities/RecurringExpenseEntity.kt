package com.example.budgetingapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.budgetingapp.domain.model.ExpenseCategory
import com.example.budgetingapp.domain.model.RecurrenceFrequency
import com.example.budgetingapp.domain.model.RecurringExpense
import java.time.LocalDate

/**
 * Room entity for the recurring_expenses table.
 *
 * This represents a template for recurring expenses.
 */
@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String, // Store as string name
    val categoryCustomName: String? = null,
    val description: String? = null,
    val frequency: String, // Store as string name
    val startDate: LocalDate,
    val nextDueDate: LocalDate,
    val isActive: Boolean = true
) {
    /**
     * Convert this entity to a domain model.
     */
    fun toDomainModel(): RecurringExpense {
        return RecurringExpense(
            id = id,
            amount = amount,
            category = ExpenseCategory.valueOf(category),
            categoryCustomName = categoryCustomName,
            description = description,
            frequency = RecurrenceFrequency.valueOf(frequency),
            startDate = startDate,
            nextDueDate = nextDueDate,
            isActive = isActive
        )
    }

    companion object {
        /**
         * Create an entity from a domain model.
         */
        fun fromDomainModel(recurringExpense: RecurringExpense): RecurringExpenseEntity {
            return RecurringExpenseEntity(
                id = recurringExpense.id,
                amount = recurringExpense.amount,
                category = recurringExpense.category.name,
                categoryCustomName = recurringExpense.categoryCustomName,
                description = recurringExpense.description,
                frequency = recurringExpense.frequency.name,
                startDate = recurringExpense.startDate,
                nextDueDate = recurringExpense.nextDueDate,
                isActive = recurringExpense.isActive
            )
        }
    }
}
