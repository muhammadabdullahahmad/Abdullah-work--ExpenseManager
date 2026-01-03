package com.example.expensesmanager

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
    private var shouldShowPinOnResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager.getInstance(this)
        enableEdgeToEdge()

        setContent {
            val viewModel: ExpenseViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            // Track if we need to show PIN unlock
            var showPinUnlock by remember { mutableStateOf(false) }

            // Check on recomposition if PIN is needed
            LaunchedEffect(shouldShowPinOnResume) {
                if (shouldShowPinOnResume && preferencesManager.isPinSet() && preferencesManager.shouldRequirePin()) {
                    showPinUnlock = true
                    shouldShowPinOnResume = false
                }
            }

            // Determine start destination based on PIN state
            val startDestination = when {
                !preferencesManager.isPinSet() -> Screen.PinWelcome.route
                preferencesManager.shouldRequirePin() || showPinUnlock -> Screen.PinUnlock.route
                else -> Screen.Home.route
            }

            ExpensesManagerTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                // Navigate to PIN unlock if needed after resume
                LaunchedEffect(showPinUnlock) {
                    if (showPinUnlock && preferencesManager.isPinSet()) {
                        navController.navigate(Screen.PinUnlock.route) {
                            popUpTo(0) { inclusive = true }
                        }
                        showPinUnlock = false
                    }
                }

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

    override fun onRestart() {
        super.onRestart()
        // Check if PIN should be required when app comes back
        if (preferencesManager.isPinSet() && preferencesManager.shouldRequirePin()) {
            shouldShowPinOnResume = true
        }
    }
}
