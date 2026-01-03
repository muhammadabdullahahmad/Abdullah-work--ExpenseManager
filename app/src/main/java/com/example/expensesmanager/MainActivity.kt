package com.example.expensesmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.expensesmanager.data.PreferencesManager
import com.example.expensesmanager.ui.navigation.NavGraph
import com.example.expensesmanager.ui.navigation.Screen
import com.example.expensesmanager.ui.theme.ExpensesManagerTheme
import com.example.expensesmanager.ui.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager.getInstance(this)
        enableEdgeToEdge()
        setContent {
            val viewModel: ExpenseViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            // Determine start destination based on PIN state
            val startDestination = when {
                !preferencesManager.isPinSet() -> Screen.PinWelcome.route
                preferencesManager.shouldRequirePin() -> Screen.PinUnlock.route
                else -> Screen.Home.route
            }

            ExpensesManagerTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    preferencesManager = preferencesManager,
                    startDestination = startDestination
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Save the time when app goes to background
        if (preferencesManager.isPinSet()) {
            preferencesManager.setLastActiveTime(System.currentTimeMillis())
        }
    }
}
