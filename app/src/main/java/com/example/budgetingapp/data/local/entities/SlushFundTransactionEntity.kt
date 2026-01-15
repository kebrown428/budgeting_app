package com.example.budgetingapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.budgetingapp.domain.model.SlushFundTransaction
import java.time.LocalDateTime

/**
 * Room entity for the slush_fund_transactions table.
 *
 * This represents manual additions/removals from the slush fund.
 */
@Entity(tableName = "slush_fund_transactions")
data class SlushFundTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val date: LocalDateTime,
    val description: String? = null
) {
    /**
     * Convert this entity to a domain model.
     */
    fun toDomainModel(): SlushFundTransaction {
        return SlushFundTransaction(
            id = id,
            amount = amount,
            date = date,
            description = description
        )
    }

    companion object {
        /**
         * Create an entity from a domain model.
         */
        fun fromDomainModel(transaction: SlushFundTransaction): SlushFundTransactionEntity {
            return SlushFundTransactionEntity(
                id = transaction.id,
                amount = transaction.amount,
                date = transaction.date,
                description = transaction.description
            )
        }
    }
}
