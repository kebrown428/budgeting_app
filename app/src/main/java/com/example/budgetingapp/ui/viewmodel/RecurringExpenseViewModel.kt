package com.example.budgetingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.budgetingapp.domain.model.ExpenseCategory
import com.example.budgetingapp.domain.model.RecurrenceFrequency
import com.example.budgetingapp.domain.model.RecurringExpense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for managing recurring expenses.
 *
 * For Sub-phase 3A, this uses mock data to demonstrate the UI.
 * In Sub-phase 3C, we'll connect this to the real repository.
 */
@HiltViewModel
class RecurringExpenseViewModel @Inject constructor(
    // TODO: Inject BudgetRepository in Sub-phase 3C
) : ViewModel() {

    // Mock data for Sub-phase 3A
    private val _recurringExpenses = MutableStateFlow(getMockData())
    val recurringExpenses: StateFlow<List<RecurringExpense>> = _recurringExpenses.asStateFlow()

    /**
     * Generate mock data for testing the UI.
     * Data is sorted by frequency: Monthly, Weekly, Bi-weekly, Annually
     */
    private fun getMockData(): List<RecurringExpense> {
        return listOf(
            RecurringExpense(
                id = 1,
                amount = 800.0,
                category = ExpenseCategory.RENT,
                description = "Apartment rent",
                frequency = RecurrenceFrequency.MONTHLY,
                startDate = LocalDate.of(2024, 1, 1),
                nextDueDate = LocalDate.of(2024, 2, 1),
                isActive = true
            ),
            RecurringExpense(
                id = 2,
                amount = 15.99,
                category = ExpenseCategory.SUBSCRIPTION,
                description = "Netflix",
                frequency = RecurrenceFrequency.MONTHLY,
                startDate = LocalDate.of(2024, 1, 15),
                nextDueDate = LocalDate.of(2024, 2, 15),
                isActive = true
            ),
            RecurringExpense(
                id = 3,
                amount = 50.0,
                category = ExpenseCategory.SUBSCRIPTION,
                description = "Phone bill",
                frequency = RecurrenceFrequency.MONTHLY,
                startDate = LocalDate.of(2024, 1, 5),
                nextDueDate = LocalDate.of(2024, 2, 5),
                isActive = true
            ),
            RecurringExpense(
                id = 4,
                amount = 75.0,
                category = ExpenseCategory.GROCERY,
                description = "Weekly groceries",
                frequency = RecurrenceFrequency.WEEKLY,
                startDate = LocalDate.of(2024, 1, 8),
                nextDueDate = LocalDate.of(2024, 1, 29),
                isActive = true
            ),
            RecurringExpense(
                id = 5,
                amount = 120.0,
                category = ExpenseCategory.NECESSITY,
                description = "Gym membership",
                frequency = RecurrenceFrequency.BI_WEEKLY,
                startDate = LocalDate.of(2024, 1, 1),
                nextDueDate = LocalDate.of(2024, 1, 29),
                isActive = true
            ),
            RecurringExpense(
                id = 6,
                amount = 600.0,
                category = ExpenseCategory.NECESSITY,
                description = "Car insurance",
                frequency = RecurrenceFrequency.ANNUALLY,
                startDate = LocalDate.of(2023, 6, 1),
                nextDueDate = LocalDate.of(2024, 6, 1),
                isActive = true
            ),
            RecurringExpense(
                id = 7,
                amount = 9.99,
                category = ExpenseCategory.SUBSCRIPTION,
                description = "Spotify (paused)",
                frequency = RecurrenceFrequency.MONTHLY,
                startDate = LocalDate.of(2024, 1, 1),
                nextDueDate = LocalDate.of(2024, 2, 1),
                isActive = false // Example of an inactive recurring expense
            )
        ).sortedWith(
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

    /**
     * Toggle the active state of a recurring expense.
     * TODO: Wire up to repository in Sub-phase 3C
     */
    fun toggleActive(expenseId: Long) {
        val currentList = _recurringExpenses.value
        _recurringExpenses.value = currentList.map { expense ->
            if (expense.id == expenseId) {
                expense.copy(isActive = !expense.isActive)
            } else {
                expense
            }
        }
    }
}
