package com.example.budgetingapp.ui.viewmodel

import app.cash.turbine.test
import com.example.budgetingapp.data.repository.FakeExpenseRepository
import com.example.budgetingapp.domain.model.Expense
import com.example.budgetingapp.domain.model.ExpenseCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeRepository = FakeExpenseRepository()
    private lateinit var viewModel: ExpenseViewModel

    // Sample test data for this week
    private val thisWeekMonday = LocalDateTime.now()
        .with(java.time.DayOfWeek.MONDAY)
        .withHour(12)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

    private val sampleGroceries = Expense(
        id = 0,
        amount = 50.0,
        category = ExpenseCategory.GROCERY,
        date = thisWeekMonday.plusDays(1), // Tuesday
        description = "Weekly groceries",
        isFromSlushFund = false,
    )

    private val sampleDining = Expense(
        id = 0,
        amount = 25.0,
        category = ExpenseCategory.DINING,
        date = thisWeekMonday.plusDays(3), // Thursday
        description = "Lunch out",
        isFromSlushFund = false,
    )

    private val sampleSlushFundExpense = Expense(
        id = 0,
        amount = 100.0,
        category = ExpenseCategory.MEDICAL,
        date = thisWeekMonday.plusDays(2), // Wednesday
        description = "Doctor visit",
        isFromSlushFund = true,
    )

    // Expense from last week
    private val lastWeekExpense = Expense(
        id = 0,
        amount = 75.0,
        category = ExpenseCategory.ENTERTAINMENT,
        date = thisWeekMonday.minusWeeks(1).plusDays(2),
        description = "Movie tickets",
        isFromSlushFund = false,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository.clear()
        viewModel = ExpenseViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows empty list for current week`() = runTest {
        viewModel.weeklyExpenses.test {
            assertEquals(emptyList<Expense>(), awaitItem())
        }
    }

    @Test
    fun `initial week offset is zero`() = runTest {
        viewModel.weekOffset.test {
            assertEquals(0, awaitItem())
        }
    }

    @Test
    fun `expenses for current week are displayed`() = runTest {
        fakeRepository.insertExpense(sampleGroceries)
        fakeRepository.insertExpense(sampleDining)
        advanceUntilIdle()

        val expenses = viewModel.weeklyExpenses.value
        assertEquals(2, expenses.size)
        assertTrue(expenses.any { it.category == ExpenseCategory.GROCERY })
        assertTrue(expenses.any { it.category == ExpenseCategory.DINING })
    }

    @Test
    fun `expenses from other weeks are not displayed`() = runTest {
        fakeRepository.insertExpense(sampleGroceries) // This week
        fakeRepository.insertExpense(lastWeekExpense) // Last week
        advanceUntilIdle()

        val expenses = viewModel.weeklyExpenses.value
        assertEquals(1, expenses.size)
        assertEquals(ExpenseCategory.GROCERY, expenses[0].category)
    }

    @Test
    fun `goToPreviousWeek changes offset and shows last week expenses`() = runTest {
        fakeRepository.insertExpense(sampleGroceries) // This week
        fakeRepository.insertExpense(lastWeekExpense) // Last week
        advanceUntilIdle()

        viewModel.goToPreviousWeek()
        advanceUntilIdle()

        assertEquals(-1, viewModel.weekOffset.value)

        val expenses = viewModel.weeklyExpenses.value
        assertEquals(1, expenses.size)
        assertEquals(ExpenseCategory.ENTERTAINMENT, expenses[0].category)
    }

    @Test
    fun `goToNextWeek changes offset`() = runTest {
        viewModel.goToNextWeek()
        advanceUntilIdle()

        assertEquals(1, viewModel.weekOffset.value)
    }

    @Test
    fun `goToCurrentWeek resets offset to zero`() = runTest {
        viewModel.goToPreviousWeek()
        viewModel.goToPreviousWeek()
        advanceUntilIdle()

        viewModel.goToCurrentWeek()
        advanceUntilIdle()

        assertEquals(0, viewModel.weekOffset.value)
    }

    @Test
    fun `setCategoryFilter filters expenses by category`() = runTest {
        fakeRepository.insertExpense(sampleGroceries)
        fakeRepository.insertExpense(sampleDining)
        advanceUntilIdle()

        viewModel.setCategoryFilter(ExpenseCategory.GROCERY)
        advanceUntilIdle()

        val expenses = viewModel.weeklyExpenses.value
        assertEquals(1, expenses.size)
        assertEquals(ExpenseCategory.GROCERY, expenses[0].category)
    }

    @Test
    fun `setCategoryFilter to null shows all expenses`() = runTest {
        fakeRepository.insertExpense(sampleGroceries)
        fakeRepository.insertExpense(sampleDining)
        advanceUntilIdle()

        // First filter
        viewModel.setCategoryFilter(ExpenseCategory.GROCERY)
        advanceUntilIdle()

        // Clear filter
        viewModel.setCategoryFilter(null)
        advanceUntilIdle()

        val expenses = viewModel.weeklyExpenses.value
        assertEquals(2, expenses.size)
    }

    @Test
    fun `weeklyTotal excludes slush fund expenses`() = runTest {
        fakeRepository.insertExpense(sampleGroceries) // $50, regular
        fakeRepository.insertExpense(sampleDining) // $25, regular
        fakeRepository.insertExpense(sampleSlushFundExpense) // $100, from slush fund
        advanceUntilIdle()

        // Should only count non-slush fund expenses: $50 + $25 = $75
        assertEquals(75.0, viewModel.weeklyTotal.value, 0.01)
    }

    @Test
    fun `addExpense creates new expense`() = runTest {
        viewModel.addExpense(
            amount = 30.0,
            category = ExpenseCategory.ENTERTAINMENT,
            date = thisWeekMonday,
            description = "Concert tickets",
            isFromSlushFund = false,
        )
        advanceUntilIdle()

        val expenses = viewModel.weeklyExpenses.value
        assertEquals(1, expenses.size)
        assertEquals(30.0, expenses[0].amount, 0.01)
        assertEquals(ExpenseCategory.ENTERTAINMENT, expenses[0].category)
        assertEquals("Concert tickets", expenses[0].description)
    }

    @Test
    fun `updateExpense modifies existing expense`() = runTest {
        fakeRepository.insertExpense(sampleGroceries)
        advanceUntilIdle()

        val expenseId = viewModel.weeklyExpenses.value[0].id

        viewModel.updateExpense(
            id = expenseId,
            amount = 60.0,
            category = ExpenseCategory.GROCERY,
            date = thisWeekMonday.plusDays(1),
            description = "More groceries",
            isFromSlushFund = false,
        )
        advanceUntilIdle()

        val expenses = viewModel.weeklyExpenses.value
        assertEquals(1, expenses.size)
        assertEquals(60.0, expenses[0].amount, 0.01)
        assertEquals("More groceries", expenses[0].description)
    }

    @Test
    fun `updateExpense does nothing for non-existent expense`() = runTest {
        fakeRepository.insertExpense(sampleGroceries)
        advanceUntilIdle()

        viewModel.updateExpense(
            id = 999,
            amount = 100.0,
            category = ExpenseCategory.OTHER,
            date = thisWeekMonday,
            description = "Should not appear",
            isFromSlushFund = false,
        )
        advanceUntilIdle()

        val expenses = viewModel.weeklyExpenses.value
        assertEquals(1, expenses.size)
        assertEquals(50.0, expenses[0].amount, 0.01) // Original amount unchanged
    }

    @Test
    fun `deleteExpense removes expense`() = runTest {
        fakeRepository.insertExpense(sampleGroceries)
        fakeRepository.insertExpense(sampleDining)
        advanceUntilIdle()

        val expenseId = viewModel.weeklyExpenses.value[0].id

        viewModel.deleteExpense(expenseId)
        advanceUntilIdle()

        val expenses = viewModel.weeklyExpenses.value
        assertEquals(1, expenses.size)
    }

    @Test
    fun `deleteExpense does nothing for non-existent expense`() = runTest {
        fakeRepository.insertExpense(sampleGroceries)
        advanceUntilIdle()

        viewModel.deleteExpense(999)
        advanceUntilIdle()

        val expenses = viewModel.weeklyExpenses.value
        assertEquals(1, expenses.size) // Original expense still there
    }

    @Test
    fun `getExpenseById returns correct expense`() = runTest {
        fakeRepository.insertExpense(sampleGroceries)
        fakeRepository.insertExpense(sampleDining)
        advanceUntilIdle()

        val expenses = viewModel.weeklyExpenses.value
        val targetId = expenses.first { it.category == ExpenseCategory.DINING }.id

        val expense = viewModel.getExpenseById(targetId)
        assertNotNull(expense)
        assertEquals(ExpenseCategory.DINING, expense?.category)
    }

    @Test
    fun `getExpenseById returns null for non-existent expense`() = runTest {
        fakeRepository.insertExpense(sampleGroceries)
        advanceUntilIdle()

        val expense = viewModel.getExpenseById(999)
        assertNull(expense)
    }

    @Test
    fun `category filter and week navigation work together`() = runTest {
        // Add expenses to this week and last week
        fakeRepository.insertExpense(sampleGroceries) // This week, GROCERY
        fakeRepository.insertExpense(sampleDining) // This week, DINING
        fakeRepository.insertExpense(lastWeekExpense) // Last week, ENTERTAINMENT
        advanceUntilIdle()

        // Filter by GROCERY category
        viewModel.setCategoryFilter(ExpenseCategory.GROCERY)
        advanceUntilIdle()

        // This week should have 1 grocery expense
        assertEquals(1, viewModel.weeklyExpenses.value.size)

        // Go to last week
        viewModel.goToPreviousWeek()
        advanceUntilIdle()

        // Last week has no grocery expenses (it's ENTERTAINMENT)
        assertEquals(0, viewModel.weeklyExpenses.value.size)

        // Clear filter
        viewModel.setCategoryFilter(null)
        advanceUntilIdle()

        // Last week should now show 1 expense
        assertEquals(1, viewModel.weeklyExpenses.value.size)
    }

    @Test
    fun `multiple operations maintain correct state`() = runTest {
        // Add multiple expenses
        viewModel.addExpense(
            amount = 40.0,
            category = ExpenseCategory.GROCERY,
            date = thisWeekMonday,
            description = "Groceries",
            isFromSlushFund = false,
        )
        advanceUntilIdle()

        viewModel.addExpense(
            amount = 20.0,
            category = ExpenseCategory.DINING,
            date = thisWeekMonday.plusDays(1),
            description = "Lunch",
            isFromSlushFund = false,
        )
        advanceUntilIdle()

        var expenses = viewModel.weeklyExpenses.value
        assertEquals(2, expenses.size)

        // Update one
        val firstId = expenses[0].id
        viewModel.updateExpense(
            id = firstId,
            amount = 45.0,
            category = ExpenseCategory.GROCERY,
            date = thisWeekMonday,
            description = "More groceries",
            isFromSlushFund = false,
        )
        advanceUntilIdle()

        val updated = viewModel.weeklyExpenses.value
        assertEquals(2, updated.size)
        assertEquals(45.0, updated.first { it.id == firstId }.amount, 0.01)

        // Delete one
        viewModel.deleteExpense(expenses[1].id)
        advanceUntilIdle()

        val afterDelete = viewModel.weeklyExpenses.value
        assertEquals(1, afterDelete.size)
        assertEquals("More groceries", afterDelete[0].description)
    }
}
