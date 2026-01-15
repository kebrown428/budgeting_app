package com.example.budgetingapp.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.budgetingapp.data.local.BudgetingDatabase
import com.example.budgetingapp.data.local.entities.ExpenseEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * Unit tests for ExpenseDao.
 *
 * Uses an in-memory database for fast, isolated tests.
 * Tests basic CRUD operations and queries.
 */
@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {

    private lateinit var database: BudgetingDatabase
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Create an in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            BudgetingDatabase::class.java
        ).allowMainThreadQueries() // Only for testing
            .build()

        expenseDao = database.expenseDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertExpense_returnsId() = runTest {
        // Given
        val expense = ExpenseEntity(
            amount = 50.0,
            category = "GROCERY",
            date = LocalDateTime.now(),
            description = "Test expense"
        )

        // When
        val id = expenseDao.insert(expense)

        // Then
        assertThat(id).isGreaterThan(0)
    }

    @Test
    fun insertAndGetExpenseById_returnsCorrectExpense() = runTest {
        // Given
        val expense = ExpenseEntity(
            amount = 50.0,
            category = "GROCERY",
            date = LocalDateTime.now(),
            description = "Test expense"
        )
        val id = expenseDao.insert(expense)

        // When & Then
        expenseDao.getExpenseById(id).test {
            val result = awaitItem()
            assertThat(result).isNotNull()
            assertThat(result?.id).isEqualTo(id)
            assertThat(result?.amount).isEqualTo(50.0)
            assertThat(result?.category).isEqualTo("GROCERY")
            assertThat(result?.description).isEqualTo("Test expense")
        }
    }

    @Test
    fun getAllExpenses_returnsAllExpensesOrderedByDate() = runTest {
        // Given
        val expense1 = ExpenseEntity(
            amount = 50.0,
            category = "GROCERY",
            date = LocalDateTime.now().minusDays(1)
        )
        val expense2 = ExpenseEntity(
            amount = 75.0,
            category = "DINING",
            date = LocalDateTime.now()
        )
        expenseDao.insert(expense1)
        expenseDao.insert(expense2)

        // When & Then
        expenseDao.getAllExpenses().test {
            val expenses = awaitItem()
            assertThat(expenses).hasSize(2)
            // Should be ordered by date DESC (newest first)
            assertThat(expenses[0].amount).isEqualTo(75.0)
            assertThat(expenses[1].amount).isEqualTo(50.0)
        }
    }

    @Test
    fun updateExpense_updatesCorrectly() = runTest {
        // Given
        val expense = ExpenseEntity(
            amount = 50.0,
            category = "GROCERY",
            date = LocalDateTime.now()
        )
        val id = expenseDao.insert(expense)

        // When
        val updatedExpense = expense.copy(id = id, amount = 100.0, category = "DINING")
        expenseDao.update(updatedExpense)

        // Then
        expenseDao.getExpenseById(id).test {
            val result = awaitItem()
            assertThat(result?.amount).isEqualTo(100.0)
            assertThat(result?.category).isEqualTo("DINING")
        }
    }

    @Test
    fun deleteExpense_removesExpense() = runTest {
        // Given
        val expense = ExpenseEntity(
            amount = 50.0,
            category = "GROCERY",
            date = LocalDateTime.now()
        )
        val id = expenseDao.insert(expense)

        // When
        expenseDao.delete(expense.copy(id = id))

        // Then
        expenseDao.getExpenseById(id).test {
            val result = awaitItem()
            assertThat(result).isNull()
        }
    }

    @Test
    fun getExpensesByDateRange_returnsOnlyExpensesInRange() = runTest {
        // Given
        val now = LocalDateTime.now()
        val expense1 = ExpenseEntity(
            amount = 50.0,
            category = "GROCERY",
            date = now.minusDays(5)
        )
        val expense2 = ExpenseEntity(
            amount = 75.0,
            category = "DINING",
            date = now.minusDays(2)
        )
        val expense3 = ExpenseEntity(
            amount = 100.0,
            category = "ENTERTAINMENT",
            date = now.minusDays(10)
        )
        expenseDao.insert(expense1)
        expenseDao.insert(expense2)
        expenseDao.insert(expense3)

        // When & Then
        val startDate = now.minusDays(7)
        val endDate = now
        expenseDao.getExpensesByDateRange(startDate, endDate).test {
            val expenses = awaitItem()
            assertThat(expenses).hasSize(2)
            assertThat(expenses.map { it.amount }).containsExactly(75.0, 50.0)
        }
    }

    @Test
    fun getTotalExpensesByDateRange_returnsCorrectSum() = runTest {
        // Given
        val now = LocalDateTime.now()
        val expense1 = ExpenseEntity(
            amount = 50.0,
            category = "GROCERY",
            date = now.minusDays(2)
        )
        val expense2 = ExpenseEntity(
            amount = 75.0,
            category = "DINING",
            date = now.minusDays(1)
        )
        val expense3 = ExpenseEntity(
            amount = 100.0,
            category = "ENTERTAINMENT",
            date = now.minusDays(10)
        )
        expenseDao.insert(expense1)
        expenseDao.insert(expense2)
        expenseDao.insert(expense3)

        // When & Then
        val startDate = now.minusDays(7)
        val endDate = now
        expenseDao.getTotalExpensesByDateRange(startDate, endDate).test {
            val total = awaitItem()
            assertThat(total).isEqualTo(125.0)
        }
    }

    @Test
    fun getExpensesFromSlushFund_returnsOnlySlushFundExpenses() = runTest {
        // Given
        val expense1 = ExpenseEntity(
            amount = 50.0,
            category = "GROCERY",
            date = LocalDateTime.now(),
            isFromSlushFund = true
        )
        val expense2 = ExpenseEntity(
            amount = 75.0,
            category = "DINING",
            date = LocalDateTime.now(),
            isFromSlushFund = false
        )
        expenseDao.insert(expense1)
        expenseDao.insert(expense2)

        // When & Then
        expenseDao.getExpensesFromSlushFund().test {
            val expenses = awaitItem()
            assertThat(expenses).hasSize(1)
            assertThat(expenses[0].amount).isEqualTo(50.0)
        }
    }
}
