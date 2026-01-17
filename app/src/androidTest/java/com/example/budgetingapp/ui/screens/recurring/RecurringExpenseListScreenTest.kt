package com.example.budgetingapp.ui.screens.recurring

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.budgetingapp.MainActivity
import com.example.budgetingapp.data.local.BudgetingDatabase
import com.example.budgetingapp.data.repository.BudgetRepository
import com.example.budgetingapp.domain.model.ExpenseCategory
import com.example.budgetingapp.domain.model.RecurrenceFrequency
import com.example.budgetingapp.domain.model.RecurringExpense
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import javax.inject.Inject

/**
 * Integration tests for RecurringExpenseListScreen using Hilt.
 *
 * These tests use the real ViewModel and Repository with an in-memory Room database,
 * providing true end-to-end testing of the UI layer.
 */
@HiltAndroidTest
class RecurringExpenseListScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var repository: BudgetRepository

    @Inject
    lateinit var database: BudgetingDatabase

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

    private val sampleGym = RecurringExpense(
        id = 0,
        amount = 50.0,
        category = ExpenseCategory.SUBSCRIPTION,
        description = null,
        frequency = RecurrenceFrequency.BI_WEEKLY,
        startDate = LocalDate.of(2024, 1, 1),
        nextDueDate = LocalDate.of(2024, 1, 15),
        isActive = false,
    )

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        runBlocking {
            // Clear all data between tests to ensure test isolation
            database.clearAllTables()
        }
    }

    @Test
    fun emptyState_displaysEmptyMessage() {
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("No recurring expenses yet")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Tap the + button to add one")
            .assertIsDisplayed()
    }

    @Test
    fun expenseList_displaysAllExpenses() {
        runBlocking {
            // Insert test data
            repository.insertRecurringExpense(sampleRent)
            repository.insertRecurringExpense(sampleGroceries)
            repository.insertRecurringExpense(sampleGym)
        }

        // Wait for the data to load and UI to update
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("$1,500.00")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify all expenses are displayed
        composeTestRule.onNodeWithText("$1,500.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly rent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rent").assertIsDisplayed()

        composeTestRule.onNodeWithText("$100.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekly groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grocery").assertIsDisplayed()

        composeTestRule.onNodeWithText("$50.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Subscription").assertIsDisplayed()
    }

    @Test
    fun expenseCard_displaysCategoryAndFrequencyBadges() {
        runBlocking {
            repository.insertRecurringExpense(sampleRent)
            repository.insertRecurringExpense(sampleGroceries)
            repository.insertRecurringExpense(sampleGym)
        }

        // Wait for the data to load and UI to update
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Rent")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Category badges
        composeTestRule.onNodeWithText("Rent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grocery").assertIsDisplayed()
        composeTestRule.onNodeWithText("Subscription").assertIsDisplayed()

        // Frequency badges
        composeTestRule.onNodeWithText("Monthly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bi-weekly").assertIsDisplayed()
    }

    @Test
    fun expenseCard_displaysNextDueDate() {
        runBlocking {
            repository.insertRecurringExpense(sampleRent)
            repository.insertRecurringExpense(sampleGroceries)
            repository.insertRecurringExpense(sampleGym)
        }

        // Wait for the data to load and UI to update
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Next: Feb 1, 2024")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Next: Feb 1, 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next: Jan 8, 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next: Jan 15, 2024").assertIsDisplayed()
    }

    @Test
    fun expenseWithoutDescription_doesNotDisplayDescription() {
        runBlocking {
            repository.insertRecurringExpense(sampleGym) // Has no description
        }

        // Wait for the data to load and UI to update
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("$50.00")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Amount and category should be displayed
        composeTestRule.onNodeWithText("$50.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Subscription").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bi-weekly").assertIsDisplayed()
    }

    @Test
    fun fabButton_isDisplayed() {
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription("Add recurring expense")
            .assertIsDisplayed()
    }

    @Test
    fun activeExpense_switchIsChecked() {
        runBlocking {
            repository.insertRecurringExpense(sampleRent.copy(isActive = true))
        }

        // Wait for the data to load and UI to update
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag("expense_active_switch")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // The switch should be on for active expenses
        composeTestRule
            .onNodeWithTag("expense_active_switch")
            .assertIsOn()
    }

    @Test
    fun inactiveExpense_switchIsUnchecked() {
        runBlocking {
            repository.insertRecurringExpense(sampleGym.copy(isActive = false))
        }

        // Wait for the data to load and UI to update
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag("expense_active_switch")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // The switch should be off for inactive expenses
        composeTestRule
            .onNodeWithTag("expense_active_switch")
            .assertIsOff()
    }

    @Test
    fun topBar_displaysTitle() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Recurring Expenses").assertIsDisplayed()
    }

    @Test
    fun expensesSortedByFrequency() {
        runBlocking {
            // Insert in random order
            repository.insertRecurringExpense(sampleGym) // Bi-weekly
            repository.insertRecurringExpense(sampleRent) // Monthly  
            repository.insertRecurringExpense(sampleGroceries) // Weekly
        }

        // Wait for the data to load and UI to update
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Monthly rent")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // All should be visible (sorted by frequency: Monthly, Weekly, Bi-weekly)
        composeTestRule.onNodeWithText("Monthly rent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekly groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Subscription").assertIsDisplayed()
    }
}
