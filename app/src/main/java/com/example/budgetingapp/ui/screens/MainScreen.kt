package com.example.budgetingapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.budgetingapp.ui.screens.expense.ExpenseListScreen
import com.example.budgetingapp.ui.screens.recurring.RecurringExpenseListScreen

/**
 * Main screen with bottom navigation bar.
 *
 * Allows switching between:
 * - Expenses (one-time expenses)
 * - Recurring (recurring expenses)
 */
@Composable
fun MainScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditExpense: (Long) -> Unit,
    onNavigateToAddRecurring: () -> Unit,
    onNavigateToEditRecurring: (Long) -> Unit,
    onNavigateToBudgetSetup: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(MainTab.EXPENSES) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                            )
                        },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag("bottom_nav_${tab.title.lowercase()}")
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                MainTab.EXPENSES -> {
                    ExpenseListScreen(
                        onAddClick = onNavigateToAddExpense,
                        onEditClick = onNavigateToEditExpense,
                        onSettingsClick = onNavigateToBudgetSetup,
                    )
                }

                MainTab.RECURRING -> {
                    RecurringExpenseListScreen(
                        onAddClick = onNavigateToAddRecurring,
                        onEditClick = onNavigateToEditRecurring,
                    )
                }
            }
        }
    }
}

/**
 * Tabs for the main screen bottom navigation.
 */
private enum class MainTab(
    val title: String,
    val icon: ImageVector,
) {
    EXPENSES("Expenses", Icons.Default.AttachMoney),
    RECURRING("Recurring", Icons.Default.Repeat),
}
