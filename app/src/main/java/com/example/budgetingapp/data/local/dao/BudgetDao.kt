package com.example.budgetingapp.data.local.dao

import androidx.room.*
import com.example.budgetingapp.data.local.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for budget operations.
 *
 * Note: There should typically only be one active budget at a time.
 */
@Dao
interface BudgetDao {

    /**
     * Insert a new budget.
     * @return The ID of the newly inserted budget
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    /**
     * Update an existing budget.
     */
    @Update
    suspend fun update(budget: BudgetEntity)

    /**
     * Delete a budget.
     */
    @Delete
    suspend fun delete(budget: BudgetEntity)

    /**
     * Get the current budget.
     * Since we expect only one budget, this returns the first one found.
     */
    @Query("SELECT * FROM budget LIMIT 1")
    fun getCurrentBudget(): Flow<BudgetEntity?>

    /**
     * Get a budget by ID.
     */
    @Query("SELECT * FROM budget WHERE id = :id")
    fun getBudgetById(id: Long): Flow<BudgetEntity?>

    /**
     * Delete all budgets.
     */
    @Query("DELETE FROM budget")
    suspend fun deleteAll()
}
