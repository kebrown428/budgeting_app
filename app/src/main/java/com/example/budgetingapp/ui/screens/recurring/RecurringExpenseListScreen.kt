package com.example.budgetingapp.ui.screens.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.budgetingapp.domain.model.RecurringExpense
import com.example.budgetingapp.ui.theme.AppTheme
import com.example.budgetingapp.ui.viewmodel.RecurringExpenseViewModel
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Screen displaying all recurring expenses.
 *
 * Features:
 * - Card-based list sorted by frequency
 * - Empty state when no expenses
 * - Toggle to activate/deactivate expenses
 * - FAB to add new recurring expense
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpenseListScreen(
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: RecurringExpenseViewModel = hiltViewModel()
) {
    val recurringExpenses by viewModel.recurringExpenses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Expenses") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add recurring expense")
            }
        }
    ) { paddingValues ->
        if (recurringExpenses.isEmpty()) {
            // Empty state
            EmptyState(modifier = Modifier.padding(paddingValues))
        } else {
            // List of recurring expenses
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = recurringExpenses,
                    key = { it.id }
                ) { expense ->
                    RecurringExpenseCard(
                        expense = expense,
                        onCardClick = { onEditClick(expense.id) },
                        onToggleActive = { viewModel.toggleActive(expense.id) }
                    )
                }
            }
        }
    }
}

/**
 * Empty state shown when there are no recurring expenses.
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "No recurring expenses yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tap the + button to add one",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Card displaying a single recurring expense.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringExpenseCard(
    expense: RecurringExpense,
    onCardClick: () -> Unit,
    onToggleActive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Card(
        onClick = onCardClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expense.isActive) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row: Amount and Active Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currencyFormat.format(expense.amount),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (expense.isActive) "Active" else "Paused",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = expense.isActive,
                        onCheckedChange = { onToggleActive() },
                        modifier = Modifier.testTag("expense_active_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category and description
            Text(
                text = expense.getCategoryDisplayName(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (!expense.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Frequency and next due date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Frequency badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    tonalElevation = 0.dp
                ) {
                    Text(
                        text = expense.frequency.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // Next due date
                Text(
                    text = "Next: ${expense.nextDueDate.format(dateFormat)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecurringExpenseListScreenPreview() {
    AppTheme {
        RecurringExpenseListScreen(
            onAddClick = {},
            onEditClick = {}
        )
    }
}
