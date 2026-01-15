package com.example.budgetingapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the Budgeting App.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection throughout the app.
 */
@HiltAndroidApp
class BudgetingApplication : Application()
