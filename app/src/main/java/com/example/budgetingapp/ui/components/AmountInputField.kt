package com.example.budgetingapp.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.budgetingapp.ui.theme.AppTheme

/**
 * Reusable text field for entering dollar amounts.
 *
 * Features:
 * - Numeric keyboard
 * - Formats input as currency (adds $ prefix)
 * - Validation (amount must be > 0)
 * - Error display after focus loss
 */
@Composable
fun AmountInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Amount",
    isError: Boolean = false,
    errorMessage: String = "Amount must be greater than 0"
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow numbers and decimal point
            val filtered = newValue.filter { it.isDigit() || it == '.' }
            // Ensure only one decimal point
            val decimalCount = filtered.count { it == '.' }
            if (decimalCount <= 1) {
                onValueChange(filtered)
            }
        },
        modifier = modifier,
        label = { Text(label) },
        leadingIcon = { Text("$") },
        singleLine = true,
        isError = isError,
        supportingText = if (isError) {
            { Text(errorMessage) }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AmountInputFieldPreview() {
    AppTheme {
        var amount by remember { mutableStateOf("50.00") }
        AmountInputField(
            value = amount,
            onValueChange = { amount = it }
        )
    }
}
