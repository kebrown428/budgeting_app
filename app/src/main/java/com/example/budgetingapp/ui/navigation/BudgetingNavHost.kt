package com.example.budgetingapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.budgetingapp.ui.screens.recurring.RecurringExpenseListScreen

/**
 * Main navigation host for the Budgeting App.
 *
 * Sets up all navigation routes and connects screens together.
 */
@Composable
fun BudgetingNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.RecurringExpenseList.route,
        modifier = modifier
    ) {
        // Recurring Expenses List Screen
        composable(Screen.RecurringExpenseList.route) {
            RecurringExpenseListScreen(
                onAddClick = {
                    navController.navigate(Screen.AddRecurringExpense.route)
                },
                onEditClick = { expenseId ->
                    navController.navigate(Screen.EditRecurringExpense.createRoute(expenseId))
                }
            )
        }

        // Add Recurring Expense Screen - placeholder for now
        composable(Screen.AddRecurringExpense.route) {
            // TODO: Implement in Sub-phase 3B
        }

        // Edit Recurring Expense Screen - placeholder for now
        composable(
            route = Screen.EditRecurringExpense.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
        ) {
            // TODO: Implement in Sub-phase 3B
        }
    }
}
