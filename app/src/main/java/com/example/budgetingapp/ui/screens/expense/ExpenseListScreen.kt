package com.example.budgetingapp.ui.screens.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.budgetingapp.domain.model.Expense
import com.example.budgetingapp.domain.model.ExpenseCategory
import com.example.budgetingapp.ui.theme.AppTheme
import com.example.budgetingapp.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Screen displaying expenses for the current week.
 *
 * Features:
 * - Weekly navigation (previous/current/next week)
 * - Category filtering
 * - Card-based expense list
 * - Total spending display
 * - FAB to add new expense
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel(),
) {
    val expenses by viewModel.weeklyExpenses.collectAsState()
    val weeklyTotal by viewModel.weeklyTotal.collectAsState()
    val weekOffset by viewModel.weekOffset.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()

    var showCategoryFilter by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                actions = {
                    IconButton(onClick = { showCategoryFilter = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter by category",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier.testTag("expenses_top_bar")
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add expense",
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Week navigation
            WeekNavigationBar(
                weekOffset = weekOffset,
                onPreviousWeek = { viewModel.goToPreviousWeek() },
                onNextWeek = { viewModel.goToNextWeek() },
                onCurrentWeek = { viewModel.goToCurrentWeek() },
            )

            // Weekly total card
            WeeklyTotalCard(
                total = weeklyTotal,
                modifier = Modifier.padding(16.dp),
            )

            // Category filter chip (if active)
            if (selectedCategory != null) {
                FilterChip(
                    selected = true,
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("${selectedCategory?.displayName} ✕") },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Expense list
            if (expenses.isEmpty()) {
                EmptyState(
                    weekOffset = weekOffset,
                    hasFilter = selectedCategory != null,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(expenses) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onClick = { onEditClick(expense.id) },
                        )
                    }
                }
            }
        }

        // Category filter dialog
        if (showCategoryFilter) {
            CategoryFilterDialog(
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    viewModel.setCategoryFilter(category)
                    showCategoryFilter = false
                },
                onDismiss = { showCategoryFilter = false },
            )
        }
    }
}

@Composable
private fun WeekNavigationBar(
    weekOffset: Int,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onCurrentWeek: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousWeek) {
                Icon(Icons.Default.ArrowBack, "Previous week")
            }

            TextButton(
                onClick = onCurrentWeek,
                enabled = weekOffset != 0,
            ) {
                Text(
                    text = when (weekOffset) {
                        0 -> "This Week"
                        -1 -> "Last Week"
                        1 -> "Next Week"
                        else -> if (weekOffset < 0) "$weekOffset weeks ago" else "$weekOffset weeks from now"
                    },
                    fontWeight = if (weekOffset == 0) FontWeight.Bold else FontWeight.Normal,
                )
            }

            IconButton(onClick = onNextWeek) {
                Icon(Icons.Default.ArrowForward, "Next week")
            }
        }
    }
}

@Composable
private fun WeeklyTotalCard(
    total: Double,
    modifier: Modifier = Modifier,
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Weekly Total (Spent)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Spent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatter.format(total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }

            // Divider
            VerticalDivider(
                modifier = Modifier
                    .height(60.dp)
                    .padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f),
            )

            // Remaining Budget (Placeholder)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Remaining",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Set Budget",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    onClick: () -> Unit,
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (expense.isFromSlushFund) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Category
                Text(
                    text = expense.getCategoryDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                // Description (if provided)
                if (!expense.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Date and time
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${expense.date.format(dateFormatter)} at ${
                        expense.date.format(
                            timeFormatter
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Slush fund badge
                if (expense.isFromSlushFund) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = "From Slush Fund",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            // Amount
            Text(
                text = formatter.format(expense.amount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun EmptyState(
    weekOffset: Int,
    hasFilter: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (hasFilter) {
                    "No expenses in this category"
                } else if (weekOffset == 0) {
                    "No expenses this week yet"
                } else {
                    "No expenses for this week"
                },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasFilter) {
                    "Clear the filter or add an expense in this category"
                } else if (weekOffset == 0) {
                    "Tap the + button to add your first expense"
                } else {
                    ""
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CategoryFilterDialog(
    selectedCategory: ExpenseCategory?,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Category") },
        text = {
            LazyColumn {
                // "All Categories" option
                item {
                    TextButton(
                        onClick = { onCategorySelected(null) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "All Categories",
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }

                // Individual categories
                items(ExpenseCategory.values().toList()) { category ->
                    TextButton(
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = category.displayName,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}
