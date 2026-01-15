package com.example.budgetingapp.data.repository

import com.example.budgetingapp.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Repository interface for expense operations.
 *
 * This abstracts the data source from the rest of the app.
 * ViewModels will depend on this interface, not the implementation.
 */
interface ExpenseRepository {

    suspend fun insertExpense(expense: Expense): Long

    suspend fun updateExpense(expense: Expense)

    suspend fun deleteExpense(expense: Expense)

    fun getExpenseById(id: Long): Flow<Expense?>

    fun getAllExpenses(): Flow<List<Expense>>

    fun getExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Expense>>

    fun getExpensesByCategory(category: String): Flow<List<Expense>>

    fun getRecurringExpenses(): Flow<List<Expense>>

    fun getExpensesFromSlushFund(): Flow<List<Expense>>

    fun getTotalExpensesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Double>

    fun getTotalExpensesByDateRangeExcludingSlushFund(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<Double>
}
