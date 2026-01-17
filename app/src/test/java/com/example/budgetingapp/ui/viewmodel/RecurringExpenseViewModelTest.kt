package com.example.budgetingapp.ui.viewmodel

import app.cash.turbine.test
import com.example.budgetingapp.data.repository.FakeBudgetRepository
import com.example.budgetingapp.domain.model.ExpenseCategory
import com.example.budgetingapp.domain.model.RecurrenceFrequency
import com.example.budgetingapp.domain.model.RecurringExpense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeRepository = FakeBudgetRepository()
    private lateinit var viewModel: RecurringExpenseViewModel

    // Sample test data
    private val sampleRent = RecurringExpense(
        id = 1,
        amount = 1500.0,
        category = ExpenseCategory.RENT,
        description = "Monthly rent",
        frequency = RecurrenceFrequency.MONTHLY,
        startDate = LocalDate.of(2024, 1, 1),
        nextDueDate = LocalDate.of(2024, 2, 1),
        isActive = true,
    )

    private val sampleGroceries = RecurringExpense(
        id = 2,
        amount = 100.0,
        category = ExpenseCategory.GROCERY,
        description = "Weekly groceries",
        frequency = RecurrenceFrequency.WEEKLY,
        startDate = LocalDate.of(2024, 1, 1),
        nextDueDate = LocalDate.of(2024, 1, 8),
        isActive = true,
    )

    private val sampleGym = RecurringExpense(
        id = 3,
        amount = 50.0,
        category = ExpenseCategory.SUBSCRIPTION,
        description = "Gym membership",
        frequency = RecurrenceFrequency.BI_WEEKLY,
        startDate = LocalDate.of(2024, 1, 1),
        nextDueDate = LocalDate.of(2024, 1, 15),
        isActive = true,
    )

    private val sampleInsurance = RecurringExpense(
        id = 4,
        amount = 1200.0,
        category = ExpenseCategory.MEDICAL,
        description = "Annual insurance",
        frequency = RecurrenceFrequency.ANNUALLY,
        startDate = LocalDate.of(2024, 1, 1),
        nextDueDate = LocalDate.of(2025, 1, 1),
        isActive = true,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository.clear()
        viewModel = RecurringExpenseViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() = runTest {
        viewModel.recurringExpenses.test {
            assertEquals(emptyList<RecurringExpense>(), awaitItem())
        }
    }

    @Test
    fun `expenses are loaded and sorted by frequency`() = runTest {
        // Add expenses in random order
        fakeRepository.insertRecurringExpense(sampleInsurance) // Annual
        fakeRepository.insertRecurringExpense(sampleGroceries) // Weekly
        fakeRepository.insertRecurringExpense(sampleRent) // Monthly
        fakeRepository.insertRecurringExpense(sampleGym) // Bi-weekly
        advanceUntilIdle()

        // Read the current value directly from the StateFlow
        val expenses = viewModel.recurringExpenses.value
        assertEquals(4, expenses.size)

        // Verify sorting: Monthly -> Weekly -> Bi-weekly -> Annually
        assertEquals(RecurrenceFrequency.MONTHLY, expenses[0].frequency)
        assertEquals(RecurrenceFrequency.WEEKLY, expenses[1].frequency)
        assertEquals(RecurrenceFrequency.BI_WEEKLY, expenses[2].frequency)
        assertEquals(RecurrenceFrequency.ANNUALLY, expenses[3].frequency)
    }

    @Test
    fun `addRecurringExpense inserts new expense`() = runTest {
        viewModel.addRecurringExpense(
            amount = 50.0,
            category = ExpenseCategory.ENTERTAINMENT,
            frequency = RecurrenceFrequency.WEEKLY,
            startDate = LocalDate.of(2024, 1, 1),
            description = "Movie night",
        )

        advanceUntilIdle()

        val expenses = viewModel.recurringExpenses.value
        assertEquals(1, expenses.size)
        assertEquals(50.0, expenses[0].amount, 0.01)
        assertEquals(ExpenseCategory.ENTERTAINMENT, expenses[0].category)
        assertEquals("Movie night", expenses[0].description)
    }

    @Test
    fun `updateRecurringExpense modifies existing expense`() = runTest {
        // Insert initial expense
        fakeRepository.insertRecurringExpense(sampleRent)
        advanceUntilIdle()

        // Update it
        viewModel.updateRecurringExpense(
            id = 1,
            amount = 1600.0,
            category = ExpenseCategory.RENT,
            frequency = RecurrenceFrequency.MONTHLY,
            startDate = LocalDate.of(2024, 1, 1),
            description = "Rent increased",
        )

        advanceUntilIdle()

        val expenses = viewModel.recurringExpenses.value
        assertEquals(1, expenses.size)
        assertEquals(1600.0, expenses[0].amount, 0.01)
        assertEquals("Rent increased", expenses[0].description)
    }

    @Test
    fun `updateRecurringExpense does nothing for non-existent expense`() = runTest {
        fakeRepository.insertRecurringExpense(sampleRent)
        advanceUntilIdle()

        // Try to update expense that doesn't exist
        viewModel.updateRecurringExpense(
            id = 999,
            amount = 100.0,
            category = ExpenseCategory.OTHER,
            frequency = RecurrenceFrequency.WEEKLY,
            startDate = LocalDate.of(2024, 1, 1),
            description = "Should not appear",
        )

        advanceUntilIdle()

        val expenses = viewModel.recurringExpenses.value
        // Original expense unchanged
        assertEquals(1, expenses.size)
        assertEquals(1500.0, expenses[0].amount, 0.01)
    }

    @Test
    fun `deleteRecurringExpense removes expense`() = runTest {
        fakeRepository.insertRecurringExpense(sampleRent)
        fakeRepository.insertRecurringExpense(sampleGroceries)
        advanceUntilIdle()

        // Delete one
        viewModel.deleteRecurringExpense(1)
        advanceUntilIdle()

        val expenses = viewModel.recurringExpenses.value
        assertEquals(1, expenses.size)
        assertEquals(2L, expenses[0].id) // Only groceries remain
    }

    @Test
    fun `deleteRecurringExpense does nothing for non-existent expense`() = runTest {
        fakeRepository.insertRecurringExpense(sampleRent)
        advanceUntilIdle()

        // Try to delete expense that doesn't exist
        viewModel.deleteRecurringExpense(999)
        advanceUntilIdle()

        val expenses = viewModel.recurringExpenses.value
        // Original expense unchanged
        assertEquals(1, expenses.size)
    }

    @Test
    fun `toggleActive changes active state`() = runTest {
        fakeRepository.insertRecurringExpense(sampleRent)
        advanceUntilIdle()

        // Toggle to inactive
        viewModel.toggleActive(1)
        advanceUntilIdle()

        var expenses = viewModel.recurringExpenses.value
        assertFalse(expenses[0].isActive)

        // Toggle back to active
        viewModel.toggleActive(1)
        advanceUntilIdle()

        expenses = viewModel.recurringExpenses.value
        assertTrue(expenses[0].isActive)
    }

    @Test
    fun `toggleActive does nothing for non-existent expense`() = runTest {
        fakeRepository.insertRecurringExpense(sampleRent)
        advanceUntilIdle()

        // Try to toggle expense that doesn't exist
        viewModel.toggleActive(999)
        advanceUntilIdle()

        val expenses = viewModel.recurringExpenses.value
        // Original expense unchanged
        assertTrue(expenses[0].isActive)
    }

    @Test
    fun `getRecurringExpenseById returns correct expense`() = runTest {
        fakeRepository.insertRecurringExpense(sampleRent)
        fakeRepository.insertRecurringExpense(sampleGroceries)
        advanceUntilIdle()

        val expense = viewModel.getRecurringExpenseById(2)
        assertNotNull(expense)
        assertEquals(sampleGroceries.description, expense?.description)
    }

    @Test
    fun `getRecurringExpenseById returns null for non-existent expense`() = runTest {
        fakeRepository.insertRecurringExpense(sampleRent)
        advanceUntilIdle()

        val expense = viewModel.getRecurringExpenseById(999)
        assertNull(expense)
    }

    @Test
    fun `expenses with same frequency are sorted by next due date`() = runTest {
        val earlyWeekly = sampleGroceries.copy(
            id = 1,
            nextDueDate = LocalDate.of(2024, 1, 8)
        )
        val lateWeekly = sampleGroceries.copy(
            id = 2,
            nextDueDate = LocalDate.of(2024, 1, 15)
        )

        fakeRepository.insertRecurringExpense(lateWeekly)
        fakeRepository.insertRecurringExpense(earlyWeekly)
        advanceUntilIdle()

        // Read the current value directly from the StateFlow
        val expenses = viewModel.recurringExpenses.value
        assertEquals(2, expenses.size)
        // Earlier due date should come first
        assertEquals(LocalDate.of(2024, 1, 8), expenses[0].nextDueDate)
        assertEquals(LocalDate.of(2024, 1, 15), expenses[1].nextDueDate)
    }

    @Test
    fun `multiple operations maintain correct state`() = runTest {
        // Add multiple expenses
        viewModel.addRecurringExpense(
            amount = 1500.0,
            category = ExpenseCategory.RENT,
            frequency = RecurrenceFrequency.MONTHLY,
            startDate = LocalDate.of(2024, 1, 1),
            description = "Rent",
        )
        advanceUntilIdle()

        viewModel.addRecurringExpense(
            amount = 100.0,
            category = ExpenseCategory.GROCERY,
            frequency = RecurrenceFrequency.WEEKLY,
            startDate = LocalDate.of(2024, 1, 1),
            description = "Groceries",
        )
        advanceUntilIdle()

        var expenses = viewModel.recurringExpenses.value
        assertEquals(2, expenses.size)

        // Update one
        viewModel.updateRecurringExpense(
            id = expenses[0].id,
            amount = 1600.0,
            category = ExpenseCategory.RENT,
            frequency = RecurrenceFrequency.MONTHLY,
            startDate = LocalDate.of(2024, 1, 1),
            description = "Rent updated",
        )
        advanceUntilIdle()

        var updated = viewModel.recurringExpenses.value
        assertEquals(2, updated.size)
        assertEquals(1600.0, updated.first { it.id == expenses[0].id }.amount, 0.01)

        // Delete one
        viewModel.deleteRecurringExpense(expenses[1].id)
        advanceUntilIdle()

        val afterDelete = viewModel.recurringExpenses.value
        assertEquals(1, afterDelete.size)
        assertEquals("Rent updated", afterDelete[0].description)
    }
}
