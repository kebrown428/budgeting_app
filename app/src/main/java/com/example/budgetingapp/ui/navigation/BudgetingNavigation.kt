package com.example.budgetingapp.ui.navigation

/**
 * Navigation routes for the Budgeting App.
 *
 * Using a sealed class ensures type-safety and makes it easy to add arguments later.
 */
sealed class Screen(val route: String) {
    // Main screen with bottom navigation
    object Main : Screen("main")

    // Recurring Expenses
    object AddRecurringExpense : Screen("add_recurring_expense")
    object EditRecurringExpense : Screen("edit_recurring_expense/{expenseId}") {
        fun createRoute(expenseId: Long): String {
            return "edit_recurring_expense/$expenseId"
        }
    }

    // One-Time Expenses
    object AddExpense : Screen("add_expense")
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: Long): String {
            return "edit_expense/$expenseId"
        }
    }
}
