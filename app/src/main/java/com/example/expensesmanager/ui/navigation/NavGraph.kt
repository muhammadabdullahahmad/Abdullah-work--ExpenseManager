package com.example.expensesmanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.expensesmanager.data.PreferencesManager
import com.example.expensesmanager.data.model.TransactionType
import com.example.expensesmanager.ui.screens.*
import com.example.expensesmanager.ui.viewmodel.ExpenseViewModel

data class BottomNavItemData(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItemData(
        route = Screen.Home.route,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItemData(
        route = Screen.Statistics.route,
        label = "Statistics",
        selectedIcon = Icons.Filled.Home, // Will use custom icon
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItemData(
        route = Screen.Settings.route,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: ExpenseViewModel,
    preferencesManager: PreferencesManager,
    startDestination: String
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if we should show bottom bar
    val showBottomBar = currentDestination?.route in listOf(
        Screen.Home.route,
        Screen.Statistics.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (currentDestination?.route == Screen.Home.route)
                                    Icons.Filled.Home
                                else
                                    Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") },
                        selected = currentDestination?.route == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (currentDestination?.route == Screen.Statistics.route)
                                    Icons.Filled.Home
                                else
                                    Icons.Outlined.Home,
                                contentDescription = "Statistics"
                            )
                        },
                        label = { Text("Statistics") },
                        selected = currentDestination?.route == Screen.Statistics.route,
                        onClick = {
                            navController.navigate(Screen.Statistics.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (currentDestination?.route == Screen.Settings.route)
                                    Icons.Filled.Settings
                                else
                                    Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        selected = currentDestination?.route == Screen.Settings.route,
                        onClick = {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            // PIN Screens
            composable(Screen.PinWelcome.route) {
                PinWelcomeScreen(
                    onNextClick = {
                        navController.navigate(Screen.PinSetup.route)
                    }
                )
            }

            composable(Screen.PinSetup.route) {
                PinSetupScreen(
                    onPinEntered = { pin ->
                        navController.navigate(Screen.PinConfirm.createRoute(pin))
                    }
                )
            }

            composable(
                route = Screen.PinConfirm.route,
                arguments = listOf(navArgument("pin") { type = NavType.StringType })
            ) { backStackEntry ->
                val pin = backStackEntry.arguments?.getString("pin") ?: ""
                PinConfirmScreen(
                    originalPin = pin,
                    onPinConfirmed = {
                        preferencesManager.setPin(pin)
                        preferencesManager.setLastActiveTime(System.currentTimeMillis())
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.PinWelcome.route) { inclusive = true }
                        }
                    },
                    onPinMismatch = {
                        // PIN mismatch handled in the screen
                    }
                )
            }

            composable(Screen.PinUnlock.route) {
                PinUnlockScreen(
                    onPinValidated = {
                        preferencesManager.setLastActiveTime(System.currentTimeMillis())
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.PinUnlock.route) { inclusive = true }
                        }
                    },
                    validatePin = { pin ->
                        preferencesManager.validatePin(pin)
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onAddExpense = { type ->
                        navController.navigate(Screen.AddExpense.createRoute(type))
                    },
                    onEditExpense = { expenseId ->
                        navController.navigate(Screen.EditExpense.createRoute(expenseId))
                    }
                )
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onChangePin = {
                        navController.navigate(Screen.ChangePin.route)
                    }
                )
            }

            composable(Screen.ChangePin.route) {
                ChangePinScreen(
                    validateCurrentPin = { pin ->
                        preferencesManager.validatePin(pin)
                    },
                    onPinChanged = { newPin ->
                        preferencesManager.setPin(newPin)
                        navController.popBackStack()
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.AddExpense.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: TransactionType.SPENDING.name
                AddExpenseScreen(
                    viewModel = viewModel,
                    transactionType = type,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditExpense.route,
                arguments = listOf(
                    navArgument("expenseId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getLong("expenseId")
                AddExpenseScreen(
                    viewModel = viewModel,
                    expenseId = expenseId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
