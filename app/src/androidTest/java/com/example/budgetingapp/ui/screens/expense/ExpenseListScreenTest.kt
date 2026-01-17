package com.example.budgetingapp.ui.screens.expense

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.budgetingapp.MainActivity
import com.example.budgetingapp.data.repository.ExpenseRepository
import com.example.budgetingapp.domain.model.Expense
import com.example.budgetingapp.domain.model.ExpenseCategory
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Integration tests for ExpenseListScreen using Hilt.
 *
 * These tests use the real ViewModel and Repository with an in-memory Room database.
 */
@HiltAndroidTest
class ExpenseListScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var repository: ExpenseRepository

    private val thisWeekMonday = LocalDateTime.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .withHour(12)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

    private val sampleGroceries = Expense(
        id = 0,
        amount = 50.0,
        category = ExpenseCategory.GROCERY,
        date = thisWeekMonday.plusDays(1),
        description = "Weekly groceries",
        isFromSlushFund = false,
    )

    private val sampleDining = Expense(
        id = 0,
        amount = 25.0,
        category = ExpenseCategory.DINING,
        date = thisWeekMonday.plusDays(2),
        description = "Lunch out",
        isFromSlushFund = false,
    )

    private val sampleSlushFundExpense = Expense(
        id = 0,
        amount = 100.0,
        category = ExpenseCategory.MEDICAL,
        date = thisWeekMonday.plusDays(3),
        description = "Doctor visit",
        isFromSlushFund = true,
    )

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        runBlocking {
            val allExpenses = repository.getAllExpenses().first()
            allExpenses.forEach { expense ->
                repository.deleteExpense(expense)
            }
        }
    }

    @Test
    fun emptyState_displaysEmptyMessage() {
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("No expenses yet this week")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Tap the + button to add your first expense")
            .assertIsDisplayed()
    }

    @Test
    fun expenseList_displaysAllExpenses() {
        runBlocking {
            repository.insertExpense(sampleGroceries)
            repository.insertExpense(sampleDining)
            repository.insertExpense(sampleSlushFundExpense)
        }

        composeTestRule.waitForIdle()

        // Verify expenses are displayed
        composeTestRule.onNodeWithText("$50.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekly groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grocery").assertIsDisplayed()

        composeTestRule.onNodeWithText("$25.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lunch out").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dining").assertIsDisplayed()

        composeTestRule.onNodeWithText("$100.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Doctor visit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Medical").assertIsDisplayed()
    }

    @Test
    fun slushFundExpense_displaysBadge() {
        runBlocking {
            repository.insertExpense(sampleSlushFundExpense)
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("From Slush Fund").assertIsDisplayed()
    }

    @Test
    fun regularExpense_doesNotDisplaySlushFundBadge() {
        runBlocking {
            repository.insertExpense(sampleGroceries)
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("From Slush Fund").assertDoesNotExist()
    }

    @Test
    fun weeklyTotalCard_displaysSpentAndRemaining() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Spent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remaining").assertIsDisplayed()
        composeTestRule.onNodeWithText("Set Budget").assertIsDisplayed()
    }

    @Test
    fun weeklyTotal_excludesSlushFundExpenses() {
        runBlocking {
            repository.insertExpense(sampleGroceries) // $50
            repository.insertExpense(sampleDining) // $25
            repository.insertExpense(sampleSlushFundExpense) // $100, from slush fund
        }

        composeTestRule.waitForIdle()

        // Weekly total should be $75 (not $175)
        composeTestRule.onNodeWithText("$75.00").assertIsDisplayed()
    }

    @Test
    fun weekNavigation_displaysThisWeekByDefault() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("This Week").assertIsDisplayed()
    }

    @Test
    fun weekNavigation_hasBackAndForwardButtons() {
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription("Previous week")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Next week")
            .assertIsDisplayed()
    }

    @Test
    fun filterButton_isDisplayed() {
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription("Filter by category")
            .assertIsDisplayed()
    }

    @Test
    fun filterButton_opensFilterDialog() {
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription("Filter by category")
            .performClick()

        composeTestRule.onNodeWithText("Filter by Category").assertIsDisplayed()
        composeTestRule.onNodeWithText("All Categories").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grocery").assertIsDisplayed()
    }

    @Test
    fun fabButton_isDisplayed() {
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription("Add expense")
            .assertIsDisplayed()
    }

    @Test
    fun topBar_displaysTitle() {
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("expenses_top_bar")
            .assertExists()
            .onChildren()
            .filterToOne(hasText("Expenses"))
            .assertIsDisplayed()
    }

    @Test
    fun expenseCard_displaysCategoryAndDescription() {
        runBlocking {
            repository.insertExpense(sampleGroceries)
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Grocery").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekly groceries").assertIsDisplayed()
    }

    @Test
    fun expenseCard_displaysDateAndTime() {
        runBlocking {
            repository.insertExpense(sampleGroceries)
        }

        composeTestRule.waitForIdle()

        // Should display date (format may vary, but should have some date text)
        composeTestRule.onNodeWithText("at 12:00 PM", substring = true).assertIsDisplayed()
    }

    @Test
    fun emptyStateWithFilter_displaysFilterMessage() {
        runBlocking {
            // Add a GROCERY expense
            repository.insertExpense(sampleGroceries)
        }

        composeTestRule.waitForIdle()

        // Apply DINING filter (which has no expenses)
        composeTestRule
            .onNodeWithContentDescription("Filter by category")
            .performClick()

        composeTestRule.onNodeWithText("Dining").performClick()

        composeTestRule.waitForIdle()

        // Should show filtered empty state
        composeTestRule.onNodeWithText("No expenses in this category").assertIsDisplayed()
    }

    @Test
    fun multipleExpenses_allDisplayedInList() {
        runBlocking {
            repository.insertExpense(sampleGroceries)
            repository.insertExpense(sampleDining)
            repository.insertExpense(sampleSlushFundExpense)
        }

        composeTestRule.waitForIdle()

        // All three expenses should be visible
        composeTestRule.onNodeWithText("Weekly groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lunch out").assertIsDisplayed()
        composeTestRule.onNodeWithText("Doctor visit").assertIsDisplayed()
    }
}
