package com.example.budgetingapp.di

import com.example.budgetingapp.data.repository.BudgetRepository
import com.example.budgetingapp.data.repository.BudgetRepositoryImpl
import com.example.budgetingapp.data.repository.ExpenseRepository
import com.example.budgetingapp.data.repository.ExpenseRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies.
 *
 * Uses @Binds instead of @Provides for better performance when binding
 * interfaces to implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds ExpenseRepositoryImpl to ExpenseRepository interface.
     *
     * When someone requests ExpenseRepository, Hilt will provide ExpenseRepositoryImpl.
     * The @Inject constructor in ExpenseRepositoryImpl tells Hilt how to create it.
     */
    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository

    /**
     * Binds BudgetRepositoryImpl to BudgetRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        budgetRepositoryImpl: BudgetRepositoryImpl
    ): BudgetRepository
}
