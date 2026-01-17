package com.example.budgetingapp.ui.screens.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.budgetingapp.R
import com.example.budgetingapp.domain.model.Expense
import com.example.budgetingapp.domain.model.ExpenseCategory
import com.example.budgetingapp.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    onSettingsClick: () -> Unit,
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
                            contentDescription = stringResource(R.string.filter_by_category),
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = stringResource(R.string.budget_setup),
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
                    contentDescription = stringResource(R.string.add_expense),
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
    // Calculate the Monday and Sunday of the displayed week
    val today = java.time.LocalDate.now()
    val monday = today
        .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        .plusWeeks(weekOffset.toLong())
    val sunday = monday.plusDays(6)

    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")
    val dateRangeText = stringResource(
        R.string.date_range,
        monday.format(dateFormatter),
        sunday.format(dateFormatter)
    )

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            // "Return to current week" button when viewing a different week
            // Using Box with fixed height to prevent layout shift
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (weekOffset != 0) {
                    OutlinedButton(
                        onClick = onCurrentWeek,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.return_to_current_week),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // Week label and navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onPreviousWeek) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.previous_week))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = when (weekOffset) {
                            0 -> stringResource(R.string.this_week)
                            -1 -> stringResource(R.string.last_week)
                            1 -> stringResource(R.string.next_week)
                            else -> if (weekOffset < 0) stringResource(
                                R.string.previous_weeks_offset,
                                weekOffset * -1
                            ) else stringResource(R.string.future_weeks_offset, weekOffset)
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = dateRangeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    )
                }

                IconButton(onClick = onNextWeek) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = stringResource(R.string.next_week)
                    )
                }
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
                    stringResource(R.string.no_expenses_ifor_category)
                } else if (weekOffset >= 0) {
                    // Current week or future weeks
                    stringResource(R.string.no_expenses_yet)
                } else {
                    // Past weeks
                    stringResource(R.string.no_expenses_past_week)
                },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasFilter) {
                    stringResource(R.string.filter_message)
                } else {
                    stringResource(R.string.add_expense_message)
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
        title = { Text(stringResource(R.string.filter_by_category)) },
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
                Text(stringResource(R.string.close))
            }
        },
    )
}
