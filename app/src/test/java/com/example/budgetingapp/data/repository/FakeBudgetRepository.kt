package com.example.budgetingapp.data.repository

import com.example.budgetingapp.domain.model.Budget
import com.example.budgetingapp.domain.model.RecurrenceFrequency
import com.example.budgetingapp.domain.model.RecurringExpense
import com.example.budgetingapp.domain.model.SlushFundTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Fake implementation of BudgetRepository for testing.
 * Uses in-memory lists and StateFlows to simulate database behavior.
 */
class FakeBudgetRepository : BudgetRepository {

    private val budgets = mutableListOf<Budget>()
    private val recurringExpenses = mutableListOf<RecurringExpense>()
    private val slushFundTransactions = mutableListOf<SlushFundTransaction>()

    private val recurringExpensesFlow = MutableStateFlow<List<RecurringExpense>>(emptyList())
    private val budgetFlow = MutableStateFlow<Budget?>(null)

    private var nextId = 1L

    // Budget operations
    override suspend fun insertBudget(budget: Budget): Long {
        val id = nextId++
        val withId = budget.copy(id = id)
        budgets.add(withId)
        budgetFlow.value = withId
        return id
    }

    override suspend fun updateBudget(budget: Budget) {
        val index = budgets.indexOfFirst { it.id == budget.id }
        if (index != -1) {
            budgets[index] = budget
            budgetFlow.value = budget
        }
    }

    override fun getCurrentBudget(): Flow<Budget?> = budgetFlow

    // Recurring expense operations
    override suspend fun insertRecurringExpense(recurringExpense: RecurringExpense): Long {
        val id = nextId++
        val withId = recurringExpense.copy(id = id)
        recurringExpenses.add(withId)
        recurringExpensesFlow.value = recurringExpenses.toList()
        return id
    }

    override suspend fun updateRecurringExpense(recurringExpense: RecurringExpense) {
        val index = recurringExpenses.indexOfFirst { it.id == recurringExpense.id }
        if (index != -1) {
            recurringExpenses[index] = recurringExpense
            recurringExpensesFlow.value = recurringExpenses.toList()
        }
    }

    override suspend fun deleteRecurringExpense(recurringExpense: RecurringExpense) {
        recurringExpenses.removeIf { it.id == recurringExpense.id }
        recurringExpensesFlow.value = recurringExpenses.toList()
    }

    override fun getAllRecurringExpenses(): Flow<List<RecurringExpense>> = recurringExpensesFlow

    override fun getActiveRecurringExpenses(): Flow<List<RecurringExpense>> {
        return recurringExpensesFlow.map { expenses ->
            expenses.filter { it.isActive }
        }
    }

    override fun getRecurringExpenseById(id: Long): Flow<RecurringExpense?> {
        return recurringExpensesFlow.map { expenses ->
            expenses.firstOrNull { it.id == id }
        }
    }

    override fun getDueRecurringExpenses(today: LocalDate): Flow<List<RecurringExpense>> {
        return recurringExpensesFlow.map { expenses ->
            expenses.filter { it.isActive && it.nextDueDate <= today }
        }
    }

    override fun getMonthlyRecurringExpenses(): Flow<List<RecurringExpense>> {
        return recurringExpensesFlow.map { expenses ->
            expenses.filter { it.frequency == RecurrenceFrequency.MONTHLY }
        }
    }

    override fun getAnnualRecurringExpenses(): Flow<List<RecurringExpense>> {
        return recurringExpensesFlow.map { expenses ->
            expenses.filter { it.frequency == RecurrenceFrequency.ANNUALLY }
        }
    }

    override fun getTotalMonthlyRecurringExpenses(): Flow<Double> {
        return recurringExpensesFlow.map { expenses ->
            expenses.filter {
                it.isActive && it.frequency == RecurrenceFrequency.MONTHLY
            }.sumOf { it.amount }
        }
    }

    // Slush fund operations
    override suspend fun insertSlushFundTransaction(transaction: SlushFundTransaction): Long {
        val id = nextId++
        val withId = transaction.copy(id = id)
        slushFundTransactions.add(withId)
        return id
    }

    override suspend fun deleteSlushFundTransaction(transaction: SlushFundTransaction) {
        slushFundTransactions.removeIf { it.id == transaction.id }
    }

    override fun getAllSlushFundTransactions(): Flow<List<SlushFundTransaction>> {
        return MutableStateFlow(slushFundTransactions.toList())
    }

    override fun getSlushFundTransactionsByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ): Flow<List<SlushFundTransaction>> {
        return MutableStateFlow(
            slushFundTransactions.filter {
                val transactionDateTime = it.date.toLocalDate().atStartOfDay()
                transactionDateTime in startDate..endDate
            }
        )
    }

    override fun getSlushFundBalance(): Flow<Double> {
        return MutableStateFlow(slushFundTransactions.sumOf { it.amount })
    }

    /**
     * Clear all data from the repository (for testing purposes).
     */
    fun clear() {
        budgets.clear()
        recurringExpenses.clear()
        slushFundTransactions.clear()
        recurringExpensesFlow.value = emptyList()
        budgetFlow.value = null
        nextId = 1L
    }
}
