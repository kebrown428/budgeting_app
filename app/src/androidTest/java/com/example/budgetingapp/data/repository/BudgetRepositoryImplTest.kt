package com.example.budgetingapp.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.budgetingapp.data.local.BudgetingDatabase
import com.example.budgetingapp.domain.model.ExpenseCategory
import com.example.budgetingapp.domain.model.RecurrenceFrequency
import com.example.budgetingapp.domain.model.RecurringExpense
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class BudgetRepositoryImplTest {

    private lateinit var database: BudgetingDatabase
    private lateinit var repository: BudgetRepositoryImpl

    private val sampleRent = RecurringExpense(
        id = 0,
        amount = 1500.0,
        category = ExpenseCategory.RENT,
        description = "Monthly rent",
        frequency = RecurrenceFrequency.MONTHLY,
        startDate = LocalDate.of(2024, 1, 1),
        nextDueDate = LocalDate.of(2024, 2, 1),
        isActive = true,
    )

    private val sampleGroceries = RecurringExpense(
        id = 0,
        amount = 100.0,
        category = ExpenseCategory.GROCERY,
        description = "Weekly groceries",
        frequency = RecurrenceFrequency.WEEKLY,
        startDate = LocalDate.of(2024, 1, 1),
        nextDueDate = LocalDate.of(2024, 1, 8),
        isActive = true,
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            BudgetingDatabase::class.java
        ).build()

        repository = BudgetRepositoryImpl(
            budgetDao = database.budgetDao(),
            recurringExpenseDao = database.recurringExpenseDao(),
            slushFundDao = database.slushFundDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertRecurringExpense_returnsValidId() = runTest {
        val id = repository.insertRecurringExpense(sampleRent)

        assertTrue(id > 0)
    }

    @Test
    fun insertRecurringExpense_canBeRetrieved() = runTest {
        val id = repository.insertRecurringExpense(sampleRent)

        repository.getRecurringExpenseById(id).test {
            val expense = awaitItem()
            assertNotNull(expense)
            assertEquals(id, expense?.id)
            assertEquals(1500.0, expense?.amount ?: 0.0, 0.01)
            assertEquals(ExpenseCategory.RENT, expense?.category)
            assertEquals("Monthly rent", expense?.description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateRecurringExpense_persistsChanges() = runTest {
        val id = repository.insertRecurringExpense(sampleRent)

        val updated = sampleRent.copy(
            id = id,
            amount = 1600.0,
            description = "Rent increased",
        )
        repository.updateRecurringExpense(updated)

        repository.getRecurringExpenseById(id).test {
            val expense = awaitItem()
            assertEquals(1600.0, expense?.amount ?: 0.0, 0.01)
            assertEquals("Rent increased", expense?.description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteRecurringExpense_removesFromDatabase() = runTest {
        val id = repository.insertRecurringExpense(sampleRent)

        val expense = sampleRent.copy(id = id)
        repository.deleteRecurringExpense(expense)

        repository.getRecurringExpenseById(id).test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllRecurringExpenses_returnsAllExpenses() = runTest {
        repository.insertRecurringExpense(sampleRent)
        repository.insertRecurringExpense(sampleGroceries)

        repository.getAllRecurringExpenses().test {
            val expenses = awaitItem()
            assertEquals(2, expenses.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllRecurringExpenses_emitsUpdatesOnInsert() = runTest {
        repository.getAllRecurringExpenses().test {
            // Initial empty state
            assertEquals(0, awaitItem().size)

            // Insert expense
            repository.insertRecurringExpense(sampleRent)

            // Flow should emit updated list
            val expenses = awaitItem()
            assertEquals(1, expenses.size)
            assertEquals(ExpenseCategory.RENT, expenses[0].category)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllRecurringExpenses_emitsUpdatesOnUpdate() = runTest {
        val id = repository.insertRecurringExpense(sampleRent)

        repository.getAllRecurringExpenses().test {
            skipItems(1) // Skip initial emission

            // Update expense
            val updated = sampleRent.copy(id = id, amount = 1700.0)
            repository.updateRecurringExpense(updated)

            // Flow should emit updated list
            val expenses = awaitItem()
            assertEquals(1700.0, expenses[0].amount, 0.01)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllRecurringExpenses_emitsUpdatesOnDelete() = runTest {
        val id = repository.insertRecurringExpense(sampleRent)

        repository.getAllRecurringExpenses().test {
            skipItems(1) // Skip initial emission

            // Delete expense
            val expense = sampleRent.copy(id = id)
            repository.deleteRecurringExpense(expense)

            // Flow should emit empty list
            assertEquals(0, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getActiveRecurringExpenses_onlyReturnsActive() = runTest {
        val activeExpense = sampleRent.copy(isActive = true)
        val inactiveExpense = sampleGroceries.copy(isActive = false)

        repository.insertRecurringExpense(activeExpense)
        repository.insertRecurringExpense(inactiveExpense)

        repository.getActiveRecurringExpenses().test {
            val expenses = awaitItem()
            assertEquals(1, expenses.size)
            assertTrue(expenses[0].isActive)
            assertEquals(ExpenseCategory.RENT, expenses[0].category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRecurringExpenseById_returnsNullForNonExistent() = runTest {
        repository.getRecurringExpenseById(999).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun multipleOperations_maintainConsistency() = runTest {
        // Insert multiple expenses
        val id1 = repository.insertRecurringExpense(sampleRent)
        val id2 = repository.insertRecurringExpense(sampleGroceries)

        repository.getAllRecurringExpenses().test {
            var expenses = awaitItem()
            assertEquals(2, expenses.size)

            // Update one
            val updated = sampleRent.copy(id = id1, amount = 1650.0)
            repository.updateRecurringExpense(updated)

            expenses = awaitItem()
            assertEquals(2, expenses.size)
            assertEquals(1650.0, expenses.find { it.id == id1 }?.amount ?: 0.0, 0.01)

            // Delete one
            repository.deleteRecurringExpense(sampleGroceries.copy(id = id2))

            expenses = awaitItem()
            assertEquals(1, expenses.size)
            assertEquals(id1, expenses[0].id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun entityConversion_preservesAllFields() = runTest {
        val expense = RecurringExpense(
            id = 0,
            amount = 250.50,
            category = ExpenseCategory.ENTERTAINMENT,
            description = "Streaming services",
            frequency = RecurrenceFrequency.MONTHLY,
            startDate = LocalDate.of(2024, 1, 15),
            nextDueDate = LocalDate.of(2024, 2, 15),
            isActive = false,
        )

        val id = repository.insertRecurringExpense(expense)

        repository.getRecurringExpenseById(id).test {
            val retrieved = awaitItem()
            assertNotNull(retrieved)
            assertEquals(250.50, retrieved?.amount ?: 0.0, 0.01)
            assertEquals(ExpenseCategory.ENTERTAINMENT, retrieved?.category)
            assertEquals("Streaming services", retrieved?.description)
            assertEquals(RecurrenceFrequency.MONTHLY, retrieved?.frequency)
            assertEquals(LocalDate.of(2024, 1, 15), retrieved?.startDate)
            assertEquals(LocalDate.of(2024, 2, 15), retrieved?.nextDueDate)
            assertFalse(retrieved?.isActive ?: true)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
