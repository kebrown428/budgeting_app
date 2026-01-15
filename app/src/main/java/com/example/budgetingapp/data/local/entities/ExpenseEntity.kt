package com.example.budgetingapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.budgetingapp.domain.model.Expense
import com.example.budgetingapp.domain.model.ExpenseCategory
import java.time.LocalDateTime

/**
 * Room entity for the expenses table.
 *
 * This represents a single expense entry in the database.
 */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String, // Store as string name for simplicity
    val categoryCustomName: String? = null,
    val date: LocalDateTime,
    val description: String? = null,
    val isFromSlushFund: Boolean = false,
    val isRecurring: Boolean = false,
    val recurringExpenseId: Long? = null
) {
    /**
     * Convert this entity to a domain model.
     */
    fun toDomainModel(): Expense {
        return Expense(
            id = id,
            amount = amount,
            category = ExpenseCategory.valueOf(category),
            categoryCustomName = categoryCustomName,
            date = date,
            description = description,
            isFromSlushFund = isFromSlushFund,
            isRecurring = isRecurring,
            recurringExpenseId = recurringExpenseId
        )
    }

    companion object {
        /**
         * Create an entity from a domain model.
         */
        fun fromDomainModel(expense: Expense): ExpenseEntity {
            return ExpenseEntity(
                id = expense.id,
                amount = expense.amount,
                category = expense.category.name,
                categoryCustomName = expense.categoryCustomName,
                date = expense.date,
                description = expense.description,
                isFromSlushFund = expense.isFromSlushFund,
                isRecurring = expense.isRecurring,
                recurringExpenseId = expense.recurringExpenseId
            )
        }
    }
}
