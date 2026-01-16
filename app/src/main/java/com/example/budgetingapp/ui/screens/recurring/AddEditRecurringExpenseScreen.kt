package com.example.budgetingapp.ui.screens.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.budgetingapp.domain.model.ExpenseCategory
import com.example.budgetingapp.domain.model.RecurrenceFrequency
import com.example.budgetingapp.ui.components.*
import com.example.budgetingapp.ui.theme.AppTheme
import com.example.budgetingapp.ui.viewmodel.RecurringExpenseViewModel
import java.time.LocalDate

/**
 * Screen for adding or editing a recurring expense.
 *
 * Features:
 * - Form with amount, category, frequency, start date, and description fields
 * - Validation with error display after field loses focus
 * - Save button (FAB)
 * - Delete button (only when editing)
 * - Confirmation dialog for delete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringExpenseScreen(
    expenseId: Long? = null, // null means "add", non-null means "edit"
    onNavigateBack: () -> Unit,
    viewModel: RecurringExpenseViewModel = hiltViewModel()
) {
    // Form state
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var selectedFrequency by remember { mutableStateOf<RecurrenceFrequency?>(null) }
    var startDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var description by remember { mutableStateOf("") }

    // Field touch state (for validation after focus loss)
    var amountTouched by remember { mutableStateOf(false) }
    var categoryTouched by remember { mutableStateOf(false) }
    var frequencyTouched by remember { mutableStateOf(false) }
    var startDateTouched by remember { mutableStateOf(false) }

    // Dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load expense data if editing
    LaunchedEffect(expenseId) {
        expenseId?.let { id ->
            viewModel.getRecurringExpenseById(id)?.let { expense ->
                amount = expense.amount.toString()
                selectedCategory = expense.category
                selectedFrequency = expense.frequency
                startDate = expense.startDate
                description = expense.description ?: ""
            }
        }
    }

    // Validation
    val isAmountValid = amount.isNotEmpty() && amount.toDoubleOrNull()?.let { it > 0 } == true
    val isCategoryValid = selectedCategory != null
    val isFrequencyValid = selectedFrequency != null
    val isStartDateValid = startDate != null
    val isFormValid = isAmountValid && isCategoryValid && isFrequencyValid && isStartDateValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (expenseId == null) "Add Recurring Expense" else "Edit Recurring Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Delete button (only when editing)
                    if (expenseId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isFormValid) {
                        if (expenseId == null) {
                            // Add new recurring expense
                            viewModel.addRecurringExpense(
                                amount = amount.toDouble(),
                                category = selectedCategory!!,
                                frequency = selectedFrequency!!,
                                startDate = startDate!!,
                                description = description.ifBlank { null }
                            )
                        } else {
                            // Update existing recurring expense
                            viewModel.updateRecurringExpense(
                                id = expenseId,
                                amount = amount.toDouble(),
                                category = selectedCategory!!,
                                frequency = selectedFrequency!!,
                                startDate = startDate!!,
                                description = description.ifBlank { null }
                            )
                        }
                        onNavigateBack()
                    }
                },
                containerColor = if (isFormValid) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Icon(Icons.Default.Done, contentDescription = "Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount field
            AmountInputField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused && !amountTouched) {
                            amountTouched = true
                        }
                    },
                isError = amountTouched && !isAmountValid,
                errorMessage = if (amount.isEmpty()) {
                    "Amount is required"
                } else {
                    "Amount must be greater than 0"
                }
            )

            // Category picker
            CategoryPicker(
                selectedCategory = selectedCategory,
                onCategorySelected = {
                    selectedCategory = it
                    categoryTouched = true
                },
                modifier = Modifier.fillMaxWidth(),
                isError = categoryTouched && !isCategoryValid
            )

            // Frequency picker
            FrequencyPicker(
                selectedFrequency = selectedFrequency,
                onFrequencySelected = {
                    selectedFrequency = it
                    frequencyTouched = true
                },
                modifier = Modifier.fillMaxWidth(),
                isError = frequencyTouched && !isFrequencyValid
            )

            // Start date picker
            DatePickerField(
                selectedDate = startDate,
                onDateSelected = {
                    startDate = it
                    startDateTouched = true
                },
                modifier = Modifier.fillMaxWidth(),
                label = "Start Date",
                isError = startDateTouched && !isStartDateValid
            )

            // Description field (optional)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Description (Optional)") },
                minLines = 3,
                maxLines = 5
            )

            // Spacer to push content up when FAB is visible
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                expenseId?.let { viewModel.deleteRecurringExpense(it) }
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddRecurringExpenseScreenPreview() {
    AppTheme {
        AddEditRecurringExpenseScreen(
            expenseId = null,
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditRecurringExpenseScreenPreview() {
    AppTheme {
        AddEditRecurringExpenseScreen(
            expenseId = 1L,
            onNavigateBack = {}
        )
    }
}
