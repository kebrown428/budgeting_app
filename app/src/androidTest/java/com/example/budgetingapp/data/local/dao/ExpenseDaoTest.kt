package com.example.budgetingapp.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.budgetingapp.data.local.BudgetingDatabase
import com.example.budgetingapp.data.local.entities.ExpenseEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
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
        Assert.assertTrue(id > 0)
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
            assertTrue(result != null)
            Assert.assertEquals(result?.id, id)
            Assert.assertEquals(result?.amount, 50.0)
            Assert.assertEquals(result?.category, "GROCERY")
            Assert.assertEquals(result?.description, "Test expense")
            cancelAndIgnoreRemainingEvents()
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
            assertTrue(expenses.size == 2)
            assertEquals(expenses[0].amount, 75.0)
            assertEquals(expenses[1].amount, 50.0)
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
            assertTrue(result != null)
            Assert.assertEquals(result?.id, id)
            Assert.assertEquals(result?.amount, 100.0)
            Assert.assertEquals(result?.category, "DINING")
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
            assertTrue(result == null)
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
            assertTrue(expenses.size == 2)
            assertEquals(expenses[0].amount, 75.0)
            assertEquals(expenses[1].amount, 50.0)
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
            assertTrue(total == 125.0)
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
            assertTrue(expenses.size == 1)
            assertEquals(expenses[0].amount, 50.0)
        }
    }
}
