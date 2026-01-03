package com.example.expensesmanager.ui.navigation

sealed class Screen(val route: String) {
    object PinWelcome : Screen("pin_welcome")
    object PinSetup : Screen("pin_setup")
    object PinConfirm : Screen("pin_confirm/{pin}") {
        fun createRoute(pin: String) = "pin_confirm/$pin"
    }
    object PinUnlock : Screen("pin_unlock")
    object ChangePin : Screen("change_pin")
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
