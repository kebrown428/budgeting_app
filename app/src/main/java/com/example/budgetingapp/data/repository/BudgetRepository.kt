package com.example.budgetingapp.data.repository

import com.example.budgetingapp.domain.model.Budget
import com.example.budgetingapp.domain.model.RecurringExpense
import com.example.budgetingapp.domain.model.SlushFundTransaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Repository interface for budget-related operations.
 *
 * This handles budget settings, recurring expenses, and slush fund transactions.
 */
interface BudgetRepository {

    // Budget operations
    suspend fun insertBudget(budget: Budget): Long

    suspend fun updateBudget(budget: Budget)

    fun getCurrentBudget(): Flow<Budget?>

    // Recurring expense operations
    suspend fun insertRecurringExpense(recurringExpense: RecurringExpense): Long

    suspend fun updateRecurringExpense(recurringExpense: RecurringExpense)

    suspend fun deleteRecurringExpense(recurringExpense: RecurringExpense)

    fun getRecurringExpenseById(id: Long): Flow<RecurringExpense?>

    fun getAllRecurringExpenses(): Flow<List<RecurringExpense>>

    fun getActiveRecurringExpenses(): Flow<List<RecurringExpense>>

    fun getDueRecurringExpenses(today: LocalDate): Flow<List<RecurringExpense>>

    fun getMonthlyRecurringExpenses(): Flow<List<RecurringExpense>>

    fun getAnnualRecurringExpenses(): Flow<List<RecurringExpense>>

    fun getTotalMonthlyRecurringExpenses(): Flow<Double>

    // Slush fund operations
    suspend fun insertSlushFundTransaction(transaction: SlushFundTransaction): Long

    suspend fun deleteSlushFundTransaction(transaction: SlushFundTransaction)

    fun getAllSlushFundTransactions(): Flow<List<SlushFundTransaction>>

    fun getSlushFundTransactionsByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<SlushFundTransaction>>

    fun getSlushFundBalance(): Flow<Double>
}
