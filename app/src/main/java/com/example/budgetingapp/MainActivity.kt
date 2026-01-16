package com.example.budgetingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.budgetingapp.ui.navigation.BudgetingNavHost
import com.example.budgetingapp.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for the Budgeting App.
 * This activity hosts the Compose UI and is set up with Hilt for dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    BudgetingNavHost(navController = navController)
                }
            }
        }
    }
}
