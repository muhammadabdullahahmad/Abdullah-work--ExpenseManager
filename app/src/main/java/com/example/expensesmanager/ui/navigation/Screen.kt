package com.example.expensesmanager.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object AddExpense : Screen("add_expense/{type}") {
        fun createRoute(type: String) = "add_expense/$type"
    }
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: Long) = "edit_expense/$expenseId"
    }
}

enum class BottomNavItem(val route: String, val label: String) {
    HOME("home", "Home"),
    STATISTICS("statistics", "Statistics"),
    SETTINGS("settings", "Settings")
}
