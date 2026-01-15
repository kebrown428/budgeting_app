package com.example.budgetingapp.di

import android.content.Context
import androidx.room.Room
import com.example.budgetingapp.data.local.BudgetingDatabase
import com.example.budgetingapp.data.local.dao.BudgetDao
import com.example.budgetingapp.data.local.dao.ExpenseDao
import com.example.budgetingapp.data.local.dao.RecurringExpenseDao
import com.example.budgetingapp.data.local.dao.SlushFundDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 *
 * @InstallIn(SingletonComponent::class) means these dependencies are available
 * throughout the entire application lifecycle.
 *
 * @Singleton ensures only one instance of the database exists.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Room database instance.
     *
     * @param context Application context provided by Hilt
     * @return The BudgetingDatabase instance
     */
    @Provides
    @Singleton
    fun provideBudgetingDatabase(
        @ApplicationContext context: Context
    ): BudgetingDatabase {
        return Room.databaseBuilder(
            context,
            BudgetingDatabase::class.java,
            "budgeting_database"
        ).build()
    }

    /**
     * Provides the ExpenseDao from the database.
     */
    @Provides
    @Singleton
    fun provideExpenseDao(database: BudgetingDatabase): ExpenseDao {
        return database.expenseDao()
    }

    /**
     * Provides the RecurringExpenseDao from the database.
     */
    @Provides
    @Singleton
    fun provideRecurringExpenseDao(database: BudgetingDatabase): RecurringExpenseDao {
        return database.recurringExpenseDao()
    }

    /**
     * Provides the BudgetDao from the database.
     */
    @Provides
    @Singleton
    fun provideBudgetDao(database: BudgetingDatabase): BudgetDao {
        return database.budgetDao()
    }

    /**
     * Provides the SlushFundDao from the database.
     */
    @Provides
    @Singleton
    fun provideSlushFundDao(database: BudgetingDatabase): SlushFundDao {
        return database.slushFundDao()
    }
}
