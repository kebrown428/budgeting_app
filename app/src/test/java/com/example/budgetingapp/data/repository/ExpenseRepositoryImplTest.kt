package com.example.budgetingapp.data.repository

import app.cash.turbine.test
import com.example.budgetingapp.data.local.dao.ExpenseDao
import com.example.budgetingapp.data.local.entities.ExpenseEntity
import com.example.budgetingapp.domain.model.Expense
import com.example.budgetingapp.domain.model.ExpenseCategory
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for ExpenseRepositoryImpl.
 *
 * Uses MockK to mock the DAO dependency, allowing us to test the repository
 * in isolation without needing a database.
 */
class ExpenseRepositoryImplTest {

    // Mock the DAO dependency
    private lateinit var expenseDao: ExpenseDao
    private lateinit var repository: ExpenseRepositoryImpl

    @Before
    fun setup() {
        expenseDao = mockk()
        repository = ExpenseRepositoryImpl(expenseDao)
    }

    @Test
    fun `insertExpense converts domain model to entity and calls DAO`() = runTest {
        // Given
        val expense = Expense(
            amount = 50.0,
            category = ExpenseCategory.GROCERY,
            date = LocalDateTime.now(),
            description = "Test expense"
        )
        val expectedId = 1L

        // Mock the DAO to return the expected ID
        coEvery { expenseDao.insert(any()) } returns expectedId

        // When
        val result = repository.insertExpense(expense)

        // Then
        assertThat(result).isEqualTo(expectedId)
        coVerify { expenseDao.insert(any()) }
    }

    @Test
    fun `getAllExpenses converts entities to domain models`() = runTest {
        // Given
        val entity1 = ExpenseEntity(
            id = 1,
            amount = 50.0,
            category = "GROCERY",
            date = LocalDateTime.now()
        )
        val entity2 = ExpenseEntity(
            id = 2,
            amount = 75.0,
            category = "DINING",
            date = LocalDateTime.now()
        )

        // Mock the DAO to return a flow of entities
        every { expenseDao.getAllExpenses() } returns flowOf(listOf(entity1, entity2))

        // When & Then
        repository.getAllExpenses().test {
            val expenses = awaitItem()
            assertThat(expenses).hasSize(2)
            assertThat(expenses[0].amount).isEqualTo(50.0)
            assertThat(expenses[0].category).isEqualTo(ExpenseCategory.GROCERY)
            assertThat(expenses[1].amount).isEqualTo(75.0)
            assertThat(expenses[1].category).isEqualTo(ExpenseCategory.DINING)
            awaitComplete()
        }
    }

    @Test
    fun `getExpenseById converts entity to domain model`() = runTest {
        // Given
        val entity = ExpenseEntity(
            id = 1,
            amount = 50.0,
            category = "GROCERY",
            date = LocalDateTime.now(),
            description = "Test"
        )

        // Mock the DAO
        every { expenseDao.getExpenseById(1L) } returns flowOf(entity)

        // When & Then
        repository.getExpenseById(1L).test {
            val expense = awaitItem()
            assertThat(expense).isNotNull()
            assertThat(expense?.id).isEqualTo(1L)
            assertThat(expense?.amount).isEqualTo(50.0)
            assertThat(expense?.category).isEqualTo(ExpenseCategory.GROCERY)
            awaitComplete()
        }
    }

    @Test
    fun `getExpenseById returns null when entity not found`() = runTest {
        // Given
        every { expenseDao.getExpenseById(999L) } returns flowOf(null)

        // When & Then
        repository.getExpenseById(999L).test {
            val expense = awaitItem()
            assertThat(expense).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `getTotalExpensesByDateRange returns correct amount`() = runTest {
        // Given
        val startDate = LocalDateTime.now().minusDays(7)
        val endDate = LocalDateTime.now()
        val expectedTotal = 125.0

        // Mock the DAO
        every {
            expenseDao.getTotalExpensesByDateRange(startDate, endDate)
        } returns flowOf(expectedTotal)

        // When & Then
        repository.getTotalExpensesByDateRange(startDate, endDate).test {
            val total = awaitItem()
            assertThat(total).isEqualTo(expectedTotal)
            awaitComplete()
        }
    }
}
