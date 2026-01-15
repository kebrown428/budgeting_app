package com.example.budgetingapp.domain.model

/**
 * Represents expense categories for budgeting.
 *
 * Includes 10 predefined categories plus the ability to use custom categories.
 * Each category has a display name used in the UI.
 */
enum class ExpenseCategory(val displayName: String) {
    RENT("Rent"),
    SUBSCRIPTION("Subscription"),
    GROCERY("Grocery"),
    MEDICAL("Medical"),
    NECESSITY("Necessity"),
    ENTERTAINMENT("Entertainment"),
    DINING("Dining"),
    TRAVEL("Travel"),
    NON_NECESSITY_GOODS("Non-necessity Goods"),
    OTHER("Other");

    companion object {
        /**
         * Get category by display name, or return OTHER if not found.
         * This allows for custom categories to fall under OTHER.
         */
        fun fromDisplayName(name: String): ExpenseCategory {
            return values().firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: OTHER
        }

        /**
         * Get all display names including the ability to add custom ones.
         */
        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }
    }
}
