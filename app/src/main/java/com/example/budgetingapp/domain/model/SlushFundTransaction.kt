package com.example.budgetingapp.domain.model

import java.time.LocalDateTime

/**
 * Represents a manual transaction to/from the slush fund.
 *
 * Note: Automatic slush fund changes (from over/under budget weeks) are calculated
 * and not stored as transactions. This is only for manual additions.
 *
 * @param id Unique identifier (0 for new transactions before saving)
 * @param amount Amount added to slush fund (positive) or removed (negative)
 * @param date Date and time of the transaction
 * @param description Optional note about why this transaction occurred
 */
data class SlushFundTransaction(
    val id: Long = 0,
    val amount: Double,
    val date: LocalDateTime,
    val description: String? = null
)
