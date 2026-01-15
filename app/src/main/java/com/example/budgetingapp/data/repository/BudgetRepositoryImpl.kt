package com.example.budgetingapp.data.repository

import com.example.budgetingapp.data.local.dao.BudgetDao
import com.example.budgetingapp.data.local.dao.RecurringExpenseDao
import com.example.budgetingapp.data.local.dao.SlushFundDao
import com.example.budgetingapp.data.local.entities.BudgetEntity
import com.example.budgetingapp.data.local.entities.RecurringExpenseEntity
import com.example.budgetingapp.data.local.entities.SlushFundTransactionEntity
import com.example.budgetingapp.domain.model.Budget
import com.example.budgetingapp.domain.model.RecurringExpense
import com.example.budgetingapp.domain.model.SlushFundTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Implementation of BudgetRepository.
 *
 * Consolidates budget, recurring expenses, and slush fund operations.
 */
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val recurringExpenseDao: RecurringExpenseDao,
    private val slushFundDao: SlushFundDao
) : BudgetRepository {

    // Budget operations
    override suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insert(BudgetEntity.fromDomainModel(budget))
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetDao.update(BudgetEntity.fromDomainModel(budget))
    }

    override fun getCurrentBudget(): Flow<Budget?> {
        return budgetDao.getCurrentBudget().map { it?.toDomainModel() }
    }

    // Recurring expense operations
    override suspend fun insertRecurringExpense(recurringExpense: RecurringExpense): Long {
        return recurringExpenseDao.insert(RecurringExpenseEntity.fromDomainModel(recurringExpense))
    }

    override suspend fun updateRecurringExpense(recurringExpense: RecurringExpense) {
        recurringExpenseDao.update(RecurringExpenseEntity.fromDomainModel(recurringExpense))
    }

    override suspend fun deleteRecurringExpense(recurringExpense: RecurringExpense) {
        recurringExpenseDao.delete(RecurringExpenseEntity.fromDomainModel(recurringExpense))
    }

    override fun getRecurringExpenseById(id: Long): Flow<RecurringExpense?> {
        return recurringExpenseDao.getRecurringExpenseById(id).map { it?.toDomainModel() }
    }

    override fun getAllRecurringExpenses(): Flow<List<RecurringExpense>> {
        return recurringExpenseDao.getAllRecurringExpenses().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getActiveRecurringExpenses(): Flow<List<RecurringExpense>> {
        return recurringExpenseDao.getActiveRecurringExpenses().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getDueRecurringExpenses(today: LocalDate): Flow<List<RecurringExpense>> {
        return recurringExpenseDao.getDueRecurringExpenses(today).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getMonthlyRecurringExpenses(): Flow<List<RecurringExpense>> {
        return recurringExpenseDao.getMonthlyRecurringExpenses().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAnnualRecurringExpenses(): Flow<List<RecurringExpense>> {
        return recurringExpenseDao.getAnnualRecurringExpenses().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTotalMonthlyRecurringExpenses(): Flow<Double> {
        return recurringExpenseDao.getTotalMonthlyRecurringExpenses()
    }

    // Slush fund operations
    override suspend fun insertSlushFundTransaction(transaction: SlushFundTransaction): Long {
        return slushFundDao.insert(SlushFundTransactionEntity.fromDomainModel(transaction))
    }

    override suspend fun deleteSlushFundTransaction(transaction: SlushFundTransaction) {
        slushFundDao.delete(SlushFundTransactionEntity.fromDomainModel(transaction))
    }

    override fun getAllSlushFundTransactions(): Flow<List<SlushFundTransaction>> {
        return slushFundDao.getAllTransactions().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getSlushFundTransactionsByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<SlushFundTransaction>> {
        return slushFundDao.getTransactionsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getSlushFundBalance(): Flow<Double> {
        return slushFundDao.getTotalBalance()
    }
}
