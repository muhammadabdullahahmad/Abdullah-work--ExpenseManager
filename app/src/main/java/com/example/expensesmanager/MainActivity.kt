package com.example.expensesmanager

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
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

        // Determine initial start destination
        val initialDestination = getStartDestination()

        setContent {
            val viewModel: ExpenseViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            ExpensesManagerTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                NavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    preferencesManager = preferencesManager,
                    startDestination = initialDestination
                )
            }
        }
    }

    private fun getStartDestination(): String {
        return when {
            !preferencesManager.isPinSet() -> Screen.PinWelcome.route
            preferencesManager.shouldRequirePin() -> Screen.PinUnlock.route
            else -> Screen.Home.route
        }
    }

    override fun onStop() {
        super.onStop()
        // Save the time when app goes to background
        if (preferencesManager.isPinSet()) {
            preferencesManager.setLastActiveTime(System.currentTimeMillis())
        }
    }

    override fun onRestart() {
        super.onRestart()
        // Check if PIN should be required when app comes back
        if (preferencesManager.isPinSet() && preferencesManager.shouldRequirePin()) {
            // Restart the activity to show PIN screen
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
