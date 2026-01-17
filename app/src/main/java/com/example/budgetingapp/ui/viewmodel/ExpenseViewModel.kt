package com.example.budgetingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetingapp.data.repository.ExpenseRepository
import com.example.budgetingapp.domain.model.Expense
import com.example.budgetingapp.domain.model.ExpenseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * ViewModel for managing one-time expenses.
 *
 * Provides weekly expense tracking with filtering by category.
 * Weeks run Monday-Sunday as per the app requirements.
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {

    // Current week offset (0 = current week, -1 = last week, 1 = next week)
    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    // Selected category filter (null = show all)
    private val _selectedCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategoryFilter: StateFlow<ExpenseCategory?> = _selectedCategoryFilter.asStateFlow()

    /**
     * Get the start of the current week (Monday at 00:00:00).
     */
    private fun getWeekStart(offset: Int = 0): LocalDateTime {
        val now = LocalDateTime.now()
        val monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        return monday.plusWeeks(offset.toLong())
    }

    /**
     * Get the end of the current week (Sunday at 23:59:59).
     */
    private fun getWeekEnd(offset: Int = 0): LocalDateTime {
        val weekStart = getWeekStart(offset)
        return weekStart.plusDays(6)
            .withHour(23)
            .withMinute(59)
            .withSecond(59)
            .withNano(999999999)
    }

    /**
     * Flow of expenses for the current week, filtered by selected category.
     */
    val weeklyExpenses: StateFlow<List<Expense>> = combine(
        _weekOffset,
        _selectedCategoryFilter,
    ) { offset, categoryFilter ->
        val startDate = getWeekStart(offset)
        val endDate = getWeekEnd(offset)

        expenseRepository.getExpensesByDateRange(startDate, endDate)
            .map { expenses ->
                if (categoryFilter == null) {
                    expenses
                } else {
                    expenses.filter { it.category == categoryFilter }
                }
            }
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    /**
     * Flow of total expenses for the current week (excluding slush fund).
     */
    val weeklyTotal: StateFlow<Double> = _weekOffset
        .flatMapLatest { offset ->
            val startDate = getWeekStart(offset)
            val endDate = getWeekEnd(offset)
            expenseRepository.getTotalExpensesByDateRangeExcludingSlushFund(startDate, endDate)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0.0,
        )

    /**
     * Navigate to the previous week.
     */
    fun goToPreviousWeek() {
        _weekOffset.value -= 1
    }

    /**
     * Navigate to the next week.
     */
    fun goToNextWeek() {
        _weekOffset.value += 1
    }

    /**
     * Reset to the current week.
     */
    fun goToCurrentWeek() {
        _weekOffset.value = 0
    }

    /**
     * Set the category filter.
     * Pass null to show all categories.
     */
    fun setCategoryFilter(category: ExpenseCategory?) {
        _selectedCategoryFilter.value = category
    }

    /**
     * Get an expense by ID.
     * Returns null if not found.
     */
    fun getExpenseById(id: Long): Expense? {
        return weeklyExpenses.value.firstOrNull { it.id == id }
    }

    /**
     * Add a new expense.
     */
    fun addExpense(
        amount: Double,
        category: ExpenseCategory,
        date: LocalDateTime,
        description: String?,
        isFromSlushFund: Boolean,
    ) {
        viewModelScope.launch {
            val newExpense = Expense(
                id = 0, // Room will auto-generate
                amount = amount,
                category = category,
                date = date,
                description = description,
                isFromSlushFund = isFromSlushFund,
                isRecurring = false,
                recurringExpenseId = null,
            )
            expenseRepository.insertExpense(newExpense)
        }
    }

    /**
     * Update an existing expense.
     */
    fun updateExpense(
        id: Long,
        amount: Double,
        category: ExpenseCategory,
        date: LocalDateTime,
        description: String?,
        isFromSlushFund: Boolean,
    ) {
        viewModelScope.launch {
            // Get the existing expense to preserve fields we're not updating
            val existing = getExpenseById(id)
            if (existing != null) {
                val updated = existing.copy(
                    amount = amount,
                    category = category,
                    date = date,
                    description = description,
                    isFromSlushFund = isFromSlushFund,
                )
                expenseRepository.updateExpense(updated)
            }
        }
    }

    /**
     * Delete an expense.
     */
    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            val expense = getExpenseById(id)
            if (expense != null) {
                expenseRepository.deleteExpense(expense)
            }
        }
    }
}
