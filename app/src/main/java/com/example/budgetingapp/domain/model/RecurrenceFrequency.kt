package com.example.budgetingapp.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Represents how often a recurring expense occurs.
 *
 * Each frequency knows how to calculate the next occurrence date from a given date.
 */
enum class RecurrenceFrequency(val displayName: String) {
    WEEKLY("Weekly") {
        override fun calculateNextDate(from: LocalDate): LocalDate = from.plusWeeks(1)
    },
    BI_WEEKLY("Bi-weekly") {
        override fun calculateNextDate(from: LocalDate): LocalDate = from.plusWeeks(2)
    },
    MONTHLY("Monthly") {
        override fun calculateNextDate(from: LocalDate): LocalDate = from.plusMonths(1)
    },
    ANNUALLY("Annually") {
        override fun calculateNextDate(from: LocalDate): LocalDate = from.plusYears(1)
    };

    /**
     * Calculate the next occurrence date from a given date.
     */
    abstract fun calculateNextDate(from: LocalDate): LocalDate

    companion object {
        fun fromDisplayName(name: String): RecurrenceFrequency? {
            return values().firstOrNull { it.displayName.equals(name, ignoreCase = true) }
        }
    }
}
