package com.example.budgetingapp.data.repository

import com.example.budgetingapp.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

/**
 * Fake implementation of ExpenseRepository for testing.
 * Uses in-memory list and StateFlow to simulate database behavior.
 */
class FakeExpenseRepository : ExpenseRepository {

    private val expenses = mutableListOf<Expense>()
    private val expensesFlow = MutableStateFlow<List<Expense>>(emptyList())
    private var nextId = 1L

    override suspend fun insertExpense(expense: Expense): Long {
        val id = nextId++
        val withId = expense.copy(id = id)
        expenses.add(withId)
        expensesFlow.value = expenses.toList()
        return id
    }

    override suspend fun updateExpense(expense: Expense) {
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index != -1) {
            expenses[index] = expense
            expensesFlow.value = expenses.toList()
        }
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenses.removeIf { it.id == expense.id }
        expensesFlow.value = expenses.toList()
    }

    override fun getExpenseById(id: Long): Flow<Expense?> {
        return expensesFlow.map { list ->
            list.firstOrNull { it.id == id }
        }
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expensesFlow
    }

    override fun getExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ): Flow<List<Expense>> {
        return expensesFlow.map { list ->
            list.filter { expense ->
                expense.date >= startDate && expense.date <= endDate
            }
        }
    }

    override fun getExpensesByCategory(category: String): Flow<List<Expense>> {
        return expensesFlow.map { list ->
            list.filter { it.category.name == category }
        }
    }

    override fun getRecurringExpenses(): Flow<List<Expense>> {
        return expensesFlow.map { list ->
            list.filter { it.isRecurring }
        }
    }

    override fun getExpensesFromSlushFund(): Flow<List<Expense>> {
        return expensesFlow.map { list ->
            list.filter { it.isFromSlushFund }
        }
    }

    override fun getTotalExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ): Flow<Double> {
        return expensesFlow.map { list ->
            list.filter { expense ->
                expense.date >= startDate && expense.date <= endDate
            }.sumOf { it.amount }
        }
    }

    override fun getTotalExpensesByDateRangeExcludingSlushFund(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ): Flow<Double> {
        return expensesFlow.map { list ->
            list.filter { expense ->
                expense.date >= startDate &&
                        expense.date <= endDate &&
                        !expense.isFromSlushFund
            }.sumOf { it.amount }
        }
    }

    /**
     * Clear all data from the repository (for testing purposes).
     */
    fun clear() {
        expenses.clear()
        expensesFlow.value = emptyList()
        nextId = 1L
    }
}
