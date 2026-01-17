package com.example.budgetingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetingapp.data.repository.BudgetRepository
import com.example.budgetingapp.domain.model.Budget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for managing budget setup and calculations.
 *
 * Responsibilities:
 * - Load and observe current budget
 * - Save/update budget settings
 * - Calculate weekly budget based on monthly budget and recurring expenses
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository,
) : ViewModel() {

    /**
     * Current budget settings (null if not set up yet).
     */
    val currentBudget: StateFlow<Budget?> = repository.getCurrentBudget()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    /**
     * Total monthly recurring expenses.
     */
    val monthlyRecurringExpenses: StateFlow<Double> = repository.getTotalMonthlyRecurringExpenses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0,
        )

    /**
     * Calculated weekly budget amount.
     * Returns null if budget is not set up yet.
     */
    val weeklyBudget: StateFlow<Double?> = combine(
        currentBudget,
        monthlyRecurringExpenses,
    ) { budget, recurringExpenses ->
        budget?.calculateWeeklyBudget(recurringExpenses)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    /**
     * Save a new budget or update existing budget.
     */
    fun saveBudget(
        monthlyAmount: Double,
        startDate: LocalDate,
    ) {
        viewModelScope.launch {
            val existingBudget = currentBudget.value
            if (existingBudget != null) {
                // Update existing budget
                repository.updateBudget(
                    existingBudget.copy(
                        monthlyAmount = monthlyAmount,
                        startDate = startDate,
                    ),
                )
            } else {
                // Insert new budget
                repository.insertBudget(
                    Budget(
                        monthlyAmount = monthlyAmount,
                        startDate = startDate,
                    ),
                )
            }
        }
    }
}
