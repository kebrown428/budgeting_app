package com.example.budgetingapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.budgetingapp.domain.model.Budget
import java.time.LocalDate

/**
 * Room entity for the budget table.
 *
 * There should typically only be one active budget at a time.
 */
@Entity(tableName = "budget")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthlyAmount: Double,
    val startDate: LocalDate
) {
    /**
     * Convert this entity to a domain model.
     */
    fun toDomainModel(): Budget {
        return Budget(
            id = id,
            monthlyAmount = monthlyAmount,
            startDate = startDate
        )
    }

    companion object {
        /**
         * Create an entity from a domain model.
         */
        fun fromDomainModel(budget: Budget): BudgetEntity {
            return BudgetEntity(
                id = budget.id,
                monthlyAmount = budget.monthlyAmount,
                startDate = budget.startDate
            )
        }
    }
}
