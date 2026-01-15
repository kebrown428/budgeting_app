package com.example.budgetingapp.data.local.dao

import androidx.room.*
import com.example.budgetingapp.data.local.entities.SlushFundTransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for slush fund transaction operations.
 */
@Dao
interface SlushFundDao {

    /**
     * Insert a new slush fund transaction.
     * @return The ID of the newly inserted transaction
     */
    @Insert
    suspend fun insert(transaction: SlushFundTransactionEntity): Long

    /**
     * Update an existing transaction.
     */
    @Update
    suspend fun update(transaction: SlushFundTransactionEntity)

    /**
     * Delete a transaction.
     */
    @Delete
    suspend fun delete(transaction: SlushFundTransactionEntity)

    /**
     * Get a transaction by ID.
     */
    @Query("SELECT * FROM slush_fund_transactions WHERE id = :id")
    fun getTransactionById(id: Long): Flow<SlushFundTransactionEntity?>

    /**
     * Get all slush fund transactions ordered by date (newest first).
     */
    @Query("SELECT * FROM slush_fund_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<SlushFundTransactionEntity>>

    /**
     * Get transactions within a date range.
     */
    @Query("SELECT * FROM slush_fund_transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<SlushFundTransactionEntity>>

    /**
     * Get the total balance of all slush fund transactions.
     * This represents manual additions/removals only.
     */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM slush_fund_transactions")
    fun getTotalBalance(): Flow<Double>

    /**
     * Delete all transactions.
     */
    @Query("DELETE FROM slush_fund_transactions")
    suspend fun deleteAll()
}
