package com.example.budgetingapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.budgetingapp.data.local.dao.BudgetDao
import com.example.budgetingapp.data.local.dao.ExpenseDao
import com.example.budgetingapp.data.local.dao.RecurringExpenseDao
import com.example.budgetingapp.data.local.dao.SlushFundDao
import com.example.budgetingapp.data.local.entities.BudgetEntity
import com.example.budgetingapp.data.local.entities.ExpenseEntity
import com.example.budgetingapp.data.local.entities.RecurringExpenseEntity
import com.example.budgetingapp.data.local.entities.SlushFundTransactionEntity

/**
 * The Room database for the Budgeting App.
 *
 * This database contains four tables:
 * - expenses: Individual expense entries
 * - recurring_expenses: Templates for recurring expenses
 * - budget: Monthly budget settings
 * - slush_fund_transactions: Manual slush fund additions/removals
 *
 * Room will automatically implement this abstract class and provide instances
 * of all the DAOs when the database is created.
 */
@Database(
    entities = [
        ExpenseEntity::class,
        RecurringExpenseEntity::class,
        BudgetEntity::class,
        SlushFundTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BudgetingDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun slushFundDao(): SlushFundDao
}
