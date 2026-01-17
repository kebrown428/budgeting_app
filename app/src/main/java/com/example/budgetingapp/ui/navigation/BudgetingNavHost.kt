package com.example.budgetingapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.budgetingapp.ui.screens.MainScreen
import com.example.budgetingapp.ui.screens.expense.AddEditExpenseScreen
import com.example.budgetingapp.ui.screens.recurring.AddEditRecurringExpenseScreen
import com.example.budgetingapp.ui.viewmodel.ExpenseViewModel
import com.example.budgetingapp.ui.viewmodel.RecurringExpenseViewModel

/**
 * Main navigation host for the Budgeting App.
 *
 * Sets up all navigation routes and connects screens together.
 * Main screen contains bottom navigation with Expenses and Recurring tabs.
 */
@Composable
fun BudgetingNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        modifier = modifier,
    ) {
        // Main Screen with bottom navigation (Expenses and Recurring tabs)
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.route)
                },
                onNavigateToEditExpense = { expenseId ->
                    navController.navigate(Screen.EditExpense.createRoute(expenseId))
                },
                onNavigateToAddRecurring = {
                    navController.navigate(Screen.AddRecurringExpense.route)
                },
                onNavigateToEditRecurring = { expenseId ->
                    navController.navigate(Screen.EditRecurringExpense.createRoute(expenseId))
                },
            )
        }

        // Add Expense Screen
        composable(Screen.AddExpense.route) {
            // ViewModel scoped to Main screen so data persists
            val viewModel: ExpenseViewModel = hiltViewModel(
                viewModelStoreOwner = navController.getBackStackEntry(Screen.Main.route),
            )
            AddEditExpenseScreen(
                expenseId = null, // null means "add mode"
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel,
            )
        }

        // Edit Expense Screen
        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType }),
        ) { backStackEntry ->
            // ViewModel scoped to Main screen so data persists
            val viewModel: ExpenseViewModel = hiltViewModel(
                viewModelStoreOwner = navController.getBackStackEntry(Screen.Main.route),
            )
            val expenseId = backStackEntry.arguments?.getLong("expenseId")
            AddEditExpenseScreen(
                expenseId = expenseId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel,
            )
        }

        // Add Recurring Expense Screen
        composable(Screen.AddRecurringExpense.route) {
            // ViewModel scoped to Main screen so data persists
            val viewModel: RecurringExpenseViewModel = hiltViewModel(
                viewModelStoreOwner = navController.getBackStackEntry(Screen.Main.route),
            )
            AddEditRecurringExpenseScreen(
                expenseId = null, // null means "add mode"
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel,
            )
        }

        // Edit Recurring Expense Screen
        composable(
            route = Screen.EditRecurringExpense.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType }),
        ) { backStackEntry ->
            // ViewModel scoped to Main screen so data persists
            val viewModel: RecurringExpenseViewModel = hiltViewModel(
                viewModelStoreOwner = navController.getBackStackEntry(Screen.Main.route),
            )
            val expenseId = backStackEntry.arguments?.getLong("expenseId")
            AddEditRecurringExpenseScreen(
                expenseId = expenseId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel,
            )
        }
    }
}
