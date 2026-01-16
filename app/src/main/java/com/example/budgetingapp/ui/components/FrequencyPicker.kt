package com.example.budgetingapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.budgetingapp.domain.model.RecurrenceFrequency
import com.example.budgetingapp.ui.theme.AppTheme

/**
 * Frequency picker using a dropdown menu.
 *
 * Since there are only 4 frequency options, a dropdown menu works well
 * and is more compact than a bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyPicker(
    selectedFrequency: RecurrenceFrequency?,
    onFrequencySelected: (RecurrenceFrequency) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Frequency",
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedFrequency?.displayName ?: "",
            onValueChange = { },
            modifier = Modifier.menuAnchor(),
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select frequency"
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

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            RecurrenceFrequency.values().forEach { frequency ->
                DropdownMenuItem(
                    text = { Text(frequency.displayName) },
                    onClick = {
                        onFrequencySelected(frequency)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FrequencyPickerPreview() {
    AppTheme {
        var selected by remember { mutableStateOf<RecurrenceFrequency?>(RecurrenceFrequency.MONTHLY) }
        FrequencyPicker(
            selectedFrequency = selected,
            onFrequencySelected = { selected = it }
        )
    }
}
