package com.example.budgetingapp.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.budgetingapp.ui.components.DatePickerField
import com.example.budgetingapp.ui.viewmodel.BudgetViewModel
import java.time.LocalDate

/**
 * Screen for setting up or editing the monthly budget.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val currentBudget by viewModel.currentBudget.collectAsState()
    val monthlyRecurringExpenses by viewModel.monthlyRecurringExpenses.collectAsState()

    // Form state
    var monthlyAmount by remember { mutableStateOf(currentBudget?.monthlyAmount?.toString() ?: "") }
    var startDate by remember { mutableStateOf(currentBudget?.startDate ?: LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Update form when budget loads
    LaunchedEffect(currentBudget) {
        currentBudget?.let { budget ->
            monthlyAmount = budget.monthlyAmount.toString()
            startDate = budget.startDate
        }
    }

    // Validation
    val monthlyAmountValue = monthlyAmount.toDoubleOrNull()
    val isValid = monthlyAmountValue != null && monthlyAmountValue > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (currentBudget == null) "Set Up Budget" else "Edit Budget") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "How It Works",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "Your monthly budget is divided into weekly amounts after subtracting recurring expenses. The remaining budget shows how much you have left to spend each week.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            // Monthly budget input
            OutlinedTextField(
                value = monthlyAmount,
                onValueChange = { monthlyAmount = it },
                label = { Text("Monthly Budget") },
                placeholder = { Text("0.00") },
                leadingIcon = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = monthlyAmount.isNotEmpty() && monthlyAmountValue == null,
                supportingText = {
                    if (monthlyAmount.isNotEmpty() && monthlyAmountValue == null) {
                        Text("Please enter a valid amount")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // Start date picker
            DatePickerField(
                selectedDate = startDate,
                onDateSelected = { startDate = it },
                label = "Budget Start Date",
                modifier = Modifier.fillMaxWidth(),
            )

            // Calculation preview
            if (monthlyAmountValue != null && monthlyAmountValue > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Budget Preview",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f),
                        )

                        BudgetPreviewRow(
                            label = "Monthly Budget",
                            amount = monthlyAmountValue,
                        )

                        BudgetPreviewRow(
                            label = "Recurring Expenses",
                            amount = -monthlyRecurringExpenses,
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f),
                        )

                        val availableAmount = monthlyAmountValue - monthlyRecurringExpenses
                        BudgetPreviewRow(
                            label = "Available for Weeks",
                            amount = availableAmount,
                            emphasized = true,
                        )

                        val weeklyAmount = availableAmount / 4.3
                        BudgetPreviewRow(
                            label = "Weekly Budget",
                            amount = weeklyAmount,
                            emphasized = true,
                        )
                    }
                }
            }

            // Save button
            Button(
                onClick = {
                    monthlyAmountValue?.let { amount ->
                        viewModel.saveBudget(
                            monthlyAmount = amount,
                            startDate = startDate,
                        )
                        onNavigateBack()
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (currentBudget == null) "Save Budget" else "Update Budget")
            }

            // Help text
            Text(
                text = "You can update your budget anytime from the settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BudgetPreviewRow(
    label: String,
    amount: Double,
    emphasized: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = if (emphasized) {
                MaterialTheme.typography.titleSmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        Text(
            text = "${"$%.2f".format(amount)}",
            style = if (emphasized) {
                MaterialTheme.typography.titleSmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = if (amount < 0) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onTertiaryContainer
            },
        )
    }
}
