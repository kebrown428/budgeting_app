package com.example.budgetingapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.budgetingapp.ui.screens.recurring.AddEditRecurringExpenseScreen
import com.example.budgetingapp.ui.screens.recurring.RecurringExpenseListScreen
import com.example.budgetingapp.ui.viewmodel.RecurringExpenseViewModel

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
        composable(Screen.RecurringExpenseList.route) { backStackEntry ->
            // Share the ViewModel across the entire NavHost by scoping it to the graph
            val viewModel: RecurringExpenseViewModel = hiltViewModel(
                viewModelStoreOwner = navController.getBackStackEntry(Screen.RecurringExpenseList.route)
            )
            RecurringExpenseListScreen(
                onAddClick = {
                    navController.navigate(Screen.AddRecurringExpense.route)
                },
                onEditClick = { expenseId ->
                    navController.navigate(Screen.EditRecurringExpense.createRoute(expenseId))
                },
                viewModel = viewModel
            )
        }

        // Add Recurring Expense Screen
        composable(Screen.AddRecurringExpense.route) {
            // Use the same ViewModel instance from the list screen
            val viewModel: RecurringExpenseViewModel = hiltViewModel(
                viewModelStoreOwner = navController.getBackStackEntry(Screen.RecurringExpenseList.route)
            )
            AddEditRecurringExpenseScreen(
                expenseId = null, // null means "add mode"
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        // Edit Recurring Expense Screen
        composable(
            route = Screen.EditRecurringExpense.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
        ) { backStackEntry ->
            // Use the same ViewModel instance from the list screen
            val viewModel: RecurringExpenseViewModel = hiltViewModel(
                viewModelStoreOwner = navController.getBackStackEntry(Screen.RecurringExpenseList.route)
            )
            val expenseId = backStackEntry.arguments?.getLong("expenseId")
            AddEditRecurringExpenseScreen(
                expenseId = expenseId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}
