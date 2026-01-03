package com.example.expensesmanager.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)

    private val _currencyCode = MutableStateFlow(prefs.getString(KEY_CURRENCY_CODE, "USD") ?: "USD")
    val currencyCode: StateFlow<String> = _currencyCode.asStateFlow()

    private val _currencySymbol = MutableStateFlow(prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setCurrency(code: String, symbol: String) {
        prefs.edit()
            .putString(KEY_CURRENCY_CODE, code)
            .putString(KEY_CURRENCY_SYMBOL, symbol)
            .apply()
        _currencyCode.value = code
        _currencySymbol.value = symbol
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled
    }

    // PIN related methods
    fun isPinSet(): Boolean {
        return prefs.getString(KEY_PIN, null) != null
    }

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun validatePin(pin: String): Boolean {
        return prefs.getString(KEY_PIN, null) == pin
    }

    fun getLastActiveTime(): Long {
        return prefs.getLong(KEY_LAST_ACTIVE_TIME, 0L)
    }

    fun setLastActiveTime(time: Long) {
        prefs.edit().putLong(KEY_LAST_ACTIVE_TIME, time).apply()
    }

    fun shouldRequirePin(): Boolean {
        if (!isPinSet()) return false
        val lastActive = getLastActiveTime()
        if (lastActive == 0L) return true
        val currentTime = System.currentTimeMillis()
        val timeout = 10 * 1000L // 10 seconds in milliseconds
        return (currentTime - lastActive) > timeout
    }

    // Lockout methods
    fun setLockoutTime(time: Long) {
        prefs.edit().putLong(KEY_LOCKOUT_TIME, time).apply()
    }

    fun getLockoutTime(): Long {
        return prefs.getLong(KEY_LOCKOUT_TIME, 0L)
    }

    fun isLockedOut(): Boolean {
        val lockoutTime = getLockoutTime()
        if (lockoutTime == 0L) return false
        val currentTime = System.currentTimeMillis()
        val oneMinute = 60 * 1000L
        return (currentTime - lockoutTime) < oneMinute
    }

    fun getRemainingLockoutSeconds(): Int {
        val lockoutTime = getLockoutTime()
        if (lockoutTime == 0L) return 0
        val currentTime = System.currentTimeMillis()
        val oneMinute = 60 * 1000L
        val remaining = oneMinute - (currentTime - lockoutTime)
        return if (remaining > 0) (remaining / 1000).toInt() else 0
    }

    fun clearLockout() {
        prefs.edit().putLong(KEY_LOCKOUT_TIME, 0L).apply()
    }

    companion object {
        private const val KEY_CURRENCY_CODE = "currency_code"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_PIN = "app_pin"
        private const val KEY_LAST_ACTIVE_TIME = "last_active_time"
        private const val KEY_LOCKOUT_TIME = "lockout_time"

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
