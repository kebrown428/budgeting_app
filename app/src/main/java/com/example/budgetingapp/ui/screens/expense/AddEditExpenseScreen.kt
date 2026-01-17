package com.example.budgetingapp.ui.screens.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.budgetingapp.domain.model.ExpenseCategory
import com.example.budgetingapp.ui.components.*
import com.example.budgetingapp.ui.theme.AppTheme
import com.example.budgetingapp.ui.viewmodel.ExpenseViewModel
import java.time.LocalDateTime

/**
 * Screen for adding or editing an expense.
 *
 * Features:
 * - Form with amount, category, date/time, description, and "from slush fund" checkbox
 * - Validation with error display after field loses focus
 * - Save button (FAB)
 * - Delete button (only when editing)
 * - Confirmation dialog for delete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    expenseId: Long? = null, // null means "add", non-null means "edit"
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel(),
) {
    // Form state
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var dateTime by remember { mutableStateOf<LocalDateTime?>(LocalDateTime.now()) }
    var description by remember { mutableStateOf("") }
    var isFromSlushFund by remember { mutableStateOf(false) }

    // Field touch state (for validation after focus loss)
    var amountTouched by remember { mutableStateOf(false) }
    var categoryTouched by remember { mutableStateOf(false) }
    var dateTimeTouched by remember { mutableStateOf(false) }

    // Dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load existing expense if editing
    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            val expense = viewModel.getExpenseById(expenseId)
            if (expense != null) {
                amount = expense.amount.toString()
                selectedCategory = expense.category
                dateTime = expense.date
                description = expense.description ?: ""
                isFromSlushFund = expense.isFromSlushFund
            }
        }
    }

    // Validation
    val amountError =
        if (amountTouched && (amount.toDoubleOrNull() == null || amount.toDoubleOrNull()!! <= 0)) {
            "Amount must be greater than 0"
        } else null

    val categoryError = if (categoryTouched && selectedCategory == null) {
        "Please select a category"
    } else null

    val dateTimeError = if (dateTimeTouched && dateTime == null) {
        "Please select date and time"
    } else null

    val isFormValid = amount.toDoubleOrNull() != null &&
            amount.toDoubleOrNull()!! > 0 &&
            selectedCategory != null &&
            dateTime != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (expenseId == null) "Add Expense" else "Edit Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
                actions = {
                    if (expenseId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete expense",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isFormValid) {
                        if (expenseId == null) {
                            // Add new expense
                            viewModel.addExpense(
                                amount = amount.toDouble(),
                                category = selectedCategory!!,
                                date = dateTime!!,
                                description = description.ifBlank { null },
                                isFromSlushFund = isFromSlushFund,
                            )
                        } else {
                            // Update existing expense
                            viewModel.updateExpense(
                                id = expenseId,
                                amount = amount.toDouble(),
                                category = selectedCategory!!,
                                date = dateTime!!,
                                description = description.ifBlank { null },
                                isFromSlushFund = isFromSlushFund,
                            )
                        }
                        onNavigateBack()
                    }
                },
                containerColor = if (isFormValid) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Save",
                    tint = if (isFormValid) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Amount
            AmountInputField(
                value = amount,
                onValueChange = { amount = it },
                isError = amountError != null,
                errorMessage = amountError ?: "Amount must be greater than 0",
                modifier = Modifier.onFocusChanged { if (!it.isFocused) amountTouched = true },
            )

            // Category
            CategoryPicker(
                selectedCategory = selectedCategory,
                onCategorySelected = {
                    selectedCategory = it
                    categoryTouched = true
                },
                isError = categoryError != null,
            )

            // Date and Time
            DateTimePickerField(
                label = "Date & Time",
                selectedDateTime = dateTime,
                onDateTimeSelected = {
                    dateTime = it
                    dateTimeTouched = true
                },
                errorMessage = dateTimeError,
            )

            // Description (optional)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
            )

            // From Slush Fund checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = isFromSlushFund,
                    onCheckedChange = { isFromSlushFund = it },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "From Slush Fund",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "This expense won't count against your weekly budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Info text about slush fund
            if (isFromSlushFund) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "💡 Slush fund expenses are tracked separately and don't affect your weekly spending goal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                title = "Delete Expense?",
                message = "This action cannot be undone.",
                onConfirm = {
                    expenseId?.let { viewModel.deleteExpense(it) }
                    showDeleteDialog = false
                    onNavigateBack()
                },
                onDismiss = { showDeleteDialog = false },
            )
        }
    }
}
