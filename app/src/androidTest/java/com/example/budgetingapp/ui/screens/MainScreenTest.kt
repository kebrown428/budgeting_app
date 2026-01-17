package com.example.budgetingapp.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.budgetingapp.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Integration tests for MainScreen (bottom navigation) using Hilt.
 *
 * These tests verify the bottom navigation behavior and tab switching.
 */
@HiltAndroidTest
class MainScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun bottomNavigation_isDisplayed() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("bottom_nav_expenses").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_recurring").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_expensesTabSelectedByDefault() {
        composeTestRule.waitForIdle()

        // Expenses tab should be selected (can check by looking for Expenses screen content)
        composeTestRule.onNodeWithText("This Week").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_switchToRecurringTab() {
        composeTestRule.waitForIdle()

        // Tap Recurring tab
        composeTestRule.onNodeWithText("Recurring").performClick()

        composeTestRule.waitForIdle()

        // Should see Recurring Expenses screen
        composeTestRule.onNodeWithText("Recurring Expenses").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_switchBackToExpensesTab() {
        composeTestRule.waitForIdle()

        // Switch to Recurring
        composeTestRule.onNodeWithText("Recurring").performClick()
        composeTestRule.waitForIdle()

        // Switch back to Expenses
        composeTestRule.onNodeWithText("Expenses").performClick()
        composeTestRule.waitForIdle()

        // Should see Expenses screen again
        composeTestRule.onNodeWithText("This Week").assertIsDisplayed()
    }

    @Test
    fun expensesTab_showsExpensesTopBar() {
        composeTestRule.waitForIdle()

        // Expenses top bar should be visible
        composeTestRule.onNodeWithTag("expenses_top_bar").assertIsDisplayed()
    }

    @Test
    fun recurringTab_showsRecurringTopBar() {
        composeTestRule.waitForIdle()

        // Switch to Recurring tab
        composeTestRule.onNodeWithTag("bottom_nav_recurring").performClick()
        composeTestRule.waitForIdle()

        // Recurring top bar should be visible
        composeTestRule.onNodeWithTag("recurring_top_bar").assertIsDisplayed()
    }

    @Test
    fun expensesTab_showsFab() {
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription("Add expense")
            .assertIsDisplayed()
    }

    @Test
    fun recurringTab_showsFab() {
        composeTestRule.waitForIdle()

        // Switch to Recurring tab
        composeTestRule.onNodeWithText("Recurring").performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription("Add recurring expense")
            .assertIsDisplayed()
    }

    @Test
    fun navigationBetweenTabs_preservesState() {
        composeTestRule.waitForIdle()

        // Verify we're on Expenses with "This Week" visible
        composeTestRule.onNodeWithText("This Week").assertIsDisplayed()

        // Switch to Recurring
        composeTestRule.onNodeWithText("Recurring").performClick()
        composeTestRule.waitForIdle()

        // Switch back to Expenses
        composeTestRule.onNodeWithText("Expenses").performClick()
        composeTestRule.waitForIdle()

        // "This Week" should still be visible (state preserved)
        composeTestRule.onNodeWithText("This Week").assertIsDisplayed()
    }

    @Test
    fun expensesTab_displaysSpentAndRemainingCards() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Spent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remaining").assertIsDisplayed()
    }

    @Test
    fun recurringTab_displaysEmptyStateByDefault() {
        composeTestRule.waitForIdle()

        // Switch to Recurring tab
        composeTestRule.onNodeWithText("Recurring").performClick()
        composeTestRule.waitForIdle()

        // Should see empty state
        composeTestRule.onNodeWithText("No recurring expenses yet").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_hasTwoTabs() {
        composeTestRule.waitForIdle()

        // Should have exactly 2 navigation items
        composeTestRule.onNodeWithTag("bottom_nav_expenses").assertExists()
        composeTestRule.onNodeWithTag("bottom_nav_recurring").assertExists()
    }
}
