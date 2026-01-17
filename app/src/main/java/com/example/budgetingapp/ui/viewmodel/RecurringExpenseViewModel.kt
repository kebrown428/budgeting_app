package com.example.budgetingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetingapp.data.repository.BudgetRepository
import com.example.budgetingapp.domain.model.ExpenseCategory
import com.example.budgetingapp.domain.model.RecurrenceFrequency
import com.example.budgetingapp.domain.model.RecurringExpense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for managing recurring expenses.
 *
 * Connected to the BudgetRepository for real data persistence.
 * All data flows reactively from the Room database.
 */
@HiltViewModel
class RecurringExpenseViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    /**
     * Flow of all recurring expenses from the database.
     * Automatically sorted by frequency and next due date.
     */
    val recurringExpenses: StateFlow<List<RecurringExpense>> = budgetRepository
        .getAllRecurringExpenses()
        .map { expenses ->
            // Sort by frequency first, then by next due date
            expenses.sortedWith(
                compareBy<RecurringExpense> {
                    when (it.frequency) {
                        RecurrenceFrequency.MONTHLY -> 0
                        RecurrenceFrequency.WEEKLY -> 1
                        RecurrenceFrequency.BI_WEEKLY -> 2
                        RecurrenceFrequency.ANNUALLY -> 3
                    }
                }.thenBy { it.nextDueDate }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    /**
     * Get a recurring expense by ID.
     * Returns null if not found.
     */
    fun getRecurringExpenseById(id: Long): RecurringExpense? {
        // Get from current state - the Flow is already keeping it updated
        return recurringExpenses.value.firstOrNull { it.id == id }
    }

    /**
     * Add a new recurring expense to the database.
     * Runs in a coroutine on the IO dispatcher.
     */
    fun addRecurringExpense(
        amount: Double,
        category: ExpenseCategory,
        frequency: RecurrenceFrequency,
        startDate: LocalDate,
        description: String?
    ) {
        viewModelScope.launch {
            val newExpense = RecurringExpense(
                id = 0, // Room will auto-generate
                amount = amount,
                category = category,
                description = description,
                frequency = frequency,
                startDate = startDate,
                nextDueDate = startDate, // Initially same as start date
                isActive = true
            )
            budgetRepository.insertRecurringExpense(newExpense)
        }
    }

    /**
     * Update an existing recurring expense in the database.
     */
    fun updateRecurringExpense(
        id: Long,
        amount: Double,
        category: ExpenseCategory,
        frequency: RecurrenceFrequency,
        startDate: LocalDate,
        description: String?
    ) {
        viewModelScope.launch {
            // Get the existing expense to preserve fields we're not updating
            val existing = recurringExpenses.value.firstOrNull { it.id == id }
            if (existing != null) {
                val updated = existing.copy(
                    amount = amount,
                    category = category,
                    description = description,
                    frequency = frequency,
                    startDate = startDate
                    // Keep nextDueDate and isActive as they were
                )
                budgetRepository.updateRecurringExpense(updated)
            }
        }
    }

    /**
     * Delete a recurring expense from the database.
     */
    fun deleteRecurringExpense(id: Long) {
        viewModelScope.launch {
            val expense = recurringExpenses.value.firstOrNull { it.id == id }
            if (expense != null) {
                budgetRepository.deleteRecurringExpense(expense)
            }
        }
    }

    /**
     * Toggle the active state of a recurring expense.
     * This allows users to pause/resume recurring expenses without deleting them.
     */
    fun toggleActive(expenseId: Long) {
        viewModelScope.launch {
            val expense = recurringExpenses.value.firstOrNull { it.id == expenseId }
            if (expense != null) {
                val updated = expense.copy(isActive = !expense.isActive)
                budgetRepository.updateRecurringExpense(updated)
            }
        }
    }
}
