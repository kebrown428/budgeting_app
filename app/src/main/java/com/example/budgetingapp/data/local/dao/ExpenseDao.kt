package com.example.budgetingapp.data.local.dao

import androidx.room.*
import com.example.budgetingapp.data.local.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for expense operations.
 *
 * All queries return Flow for reactive updates, except for insert/update/delete operations.
 */
@Dao
interface ExpenseDao {

    /**
     * Insert a new expense.
     * @return The ID of the newly inserted expense
     */
    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    /**
     * Update an existing expense.
     */
    @Update
    suspend fun update(expense: ExpenseEntity)

    /**
     * Delete an expense.
     */
    @Delete
    suspend fun delete(expense: ExpenseEntity)

    /**
     * Get an expense by ID.
     */
    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    fun getExpenseById(expenseId: Long): Flow<ExpenseEntity?>

    /**
     * Get all expenses ordered by date (newest first).
     */
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    /**
     * Get expenses within a date range.
     * @param startDate Start of the date range (inclusive)
     * @param endDate End of the date range (inclusive)
     */
    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<ExpenseEntity>>

    /**
     * Get expenses for a specific category.
     */
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>>

    /**
     * Get all recurring expenses.
     */
    @Query("SELECT * FROM expenses WHERE isRecurring = 1 ORDER BY date DESC")
    fun getRecurringExpenses(): Flow<List<ExpenseEntity>>

    /**
     * Get expenses paid from slush fund.
     */
    @Query("SELECT * FROM expenses WHERE isFromSlushFund = 1 ORDER BY date DESC")
    fun getExpensesFromSlushFund(): Flow<List<ExpenseEntity>>

    /**
     * Get total expense amount for a date range.
     */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE date >= :startDate AND date <= :endDate")
    fun getTotalExpensesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Double>

    /**
     * Get total expense amount for a date range, excluding expenses from slush fund.
     */
    @Query(
        """
        SELECT COALESCE(SUM(amount), 0.0) FROM expenses 
        WHERE date >= :startDate AND date <= :endDate AND isFromSlushFund = 0
    """
    )
    fun getTotalExpensesByDateRangeExcludingSlushFund(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<Double>

    /**
     * Delete all expenses.
     */
    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}
