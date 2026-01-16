package com.example.budgetingapp.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.budgetingapp.ui.theme.AppTheme

/**
 * Reusable confirmation dialog for delete actions.
 *
 * Shows a warning message and asks for confirmation before proceeding with deletion.
 */
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "Delete Recurring Expense?",
    message: String = "This action cannot be undone. Are you sure you want to delete this recurring expense?"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview
@Composable
private fun DeleteConfirmationDialogPreview() {
    AppTheme {
        DeleteConfirmationDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
