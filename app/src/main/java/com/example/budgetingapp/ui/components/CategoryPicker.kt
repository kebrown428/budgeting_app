package com.example.budgetingapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.budgetingapp.domain.model.ExpenseCategory
import com.example.budgetingapp.ui.theme.AppTheme

/**
 * Category picker using a bottom sheet to display all available categories.
 *
 * Shows the selected category in an outlined text field,
 * opens a bottom sheet when clicked to select a different category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPicker(
    selectedCategory: ExpenseCategory?,
    onCategorySelected: (ExpenseCategory) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Category",
    isError: Boolean = false
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    // Clickable text field to open bottom sheet
    OutlinedTextField(
        value = selectedCategory?.displayName ?: "",
        onValueChange = { }, // Read-only
        modifier = modifier.clickable { showBottomSheet = true },
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select category"
            )
        },
        readOnly = true,
        enabled = false,
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.outline
            },
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )

    // Bottom sheet with all categories
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                LazyColumn {
                    items(ExpenseCategory.entries.toTypedArray()) { category ->
                        ListItem(
                            headlineContent = { Text(category.displayName) },
                            modifier = Modifier.clickable {
                                onCategorySelected(category)
                                showBottomSheet = false
                            }
                        )
                        if (category != ExpenseCategory.entries.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryPickerPreview() {
    AppTheme {
        var selected by remember { mutableStateOf<ExpenseCategory?>(ExpenseCategory.GROCERY) }
        CategoryPicker(
            selectedCategory = selected,
            onCategorySelected = { selected = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
