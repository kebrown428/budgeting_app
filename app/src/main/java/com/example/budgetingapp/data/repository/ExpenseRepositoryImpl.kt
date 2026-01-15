package com.example.budgetingapp.data.repository

import com.example.budgetingapp.data.local.dao.ExpenseDao
import com.example.budgetingapp.data.local.entities.ExpenseEntity
import com.example.budgetingapp.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Implementation of ExpenseRepository.
 *
 * Converts between domain models and database entities.
 * Uses Hilt's @Inject constructor for dependency injection.
 */
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insert(ExpenseEntity.fromDomainModel(expense))
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.update(ExpenseEntity.fromDomainModel(expense))
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(ExpenseEntity.fromDomainModel(expense))
    }

    override fun getExpenseById(id: Long): Flow<Expense?> {
        return expenseDao.getExpenseById(id).map { it?.toDomainModel() }
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Expense>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getExpensesByCategory(category: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByCategory(category).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getRecurringExpenses(): Flow<List<Expense>> {
        return expenseDao.getRecurringExpenses().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getExpensesFromSlushFund(): Flow<List<Expense>> {
        return expenseDao.getExpensesFromSlushFund().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTotalExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<Double> {
        return expenseDao.getTotalExpensesByDateRange(startDate, endDate)
    }

    override fun getTotalExpensesByDateRangeExcludingSlushFund(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<Double> {
        return expenseDao.getTotalExpensesByDateRangeExcludingSlushFund(startDate, endDate)
    }
}
