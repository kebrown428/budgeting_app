package com.example.budgetingapp.ui.navigation

/**
 * Navigation routes for the Budgeting App.
 *
 * Using a sealed class ensures type-safety and makes it easy to add arguments later.
 */
sealed class Screen(val route: String) {
    object RecurringExpenseList : Screen("recurring_expense_list")
    object AddRecurringExpense : Screen("add_recurring_expense")

    // route with argument for editing
    object EditRecurringExpense : Screen("edit_recurring_expense/{expenseId}") {
        fun createRoute(expenseId: Long): String {
            return "edit_recurring_expense/$expenseId"
        }
    }
}
