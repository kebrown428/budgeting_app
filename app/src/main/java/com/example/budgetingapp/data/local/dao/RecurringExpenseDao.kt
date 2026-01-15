package com.example.budgetingapp.data.local.dao

import androidx.room.*
import com.example.budgetingapp.data.local.entities.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for recurring expense operations.
 */
@Dao
interface RecurringExpenseDao {

    /**
     * Insert a new recurring expense.
     * @return The ID of the newly inserted recurring expense
     */
    @Insert
    suspend fun insert(recurringExpense: RecurringExpenseEntity): Long

    /**
     * Update an existing recurring expense.
     */
    @Update
    suspend fun update(recurringExpense: RecurringExpenseEntity)

    /**
     * Delete a recurring expense.
     */
    @Delete
    suspend fun delete(recurringExpense: RecurringExpenseEntity)

    /**
     * Get a recurring expense by ID.
     */
    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    fun getRecurringExpenseById(id: Long): Flow<RecurringExpenseEntity?>

    /**
     * Get all recurring expenses.
     */
    @Query("SELECT * FROM recurring_expenses ORDER BY nextDueDate ASC")
    fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    /**
     * Get all active recurring expenses.
     */
    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    /**
     * Get recurring expenses that are due (nextDueDate <= today).
     */
    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1 AND nextDueDate <= :today ORDER BY nextDueDate ASC")
    fun getDueRecurringExpenses(today: LocalDate): Flow<List<RecurringExpenseEntity>>

    /**
     * Get monthly recurring expenses (for budget calculation).
     */
    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1 AND frequency = 'MONTHLY'")
    fun getMonthlyRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    /**
     * Get annual recurring expenses (for reminders).
     */
    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1 AND frequency = 'ANNUALLY' ORDER BY nextDueDate ASC")
    fun getAnnualRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    /**
     * Get total amount of monthly recurring expenses.
     */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM recurring_expenses WHERE isActive = 1 AND frequency = 'MONTHLY'")
    fun getTotalMonthlyRecurringExpenses(): Flow<Double>

    /**
     * Delete all recurring expenses.
     */
    @Query("DELETE FROM recurring_expenses")
    suspend fun deleteAll()
}
