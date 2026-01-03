package com.example.expensesmanager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensesmanager.data.AppDatabase
import com.example.expensesmanager.data.PreferencesManager
import com.example.expensesmanager.data.dao.CategoryTotal
import com.example.expensesmanager.data.dao.DebtPersonTotal
import com.example.expensesmanager.data.model.Category
import com.example.expensesmanager.data.model.Expense
import com.example.expensesmanager.data.model.TransactionType
import com.example.expensesmanager.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository
    val preferencesManager: PreferencesManager

    val spendingCategories: StateFlow<List<Category>>
    val earningCategories: StateFlow<List<Category>>
    val debtCategories: StateFlow<List<Category>>
    val allCategories: StateFlow<List<Category>>

    val currentMonthSpending: StateFlow<List<Expense>>
    val currentMonthEarnings: StateFlow<List<Expense>>
    val currentMonthDebt: StateFlow<List<Expense>>

    val totalSpending: StateFlow<Double>
    val totalEarnings: StateFlow<Double>
    val totalDebt: StateFlow<Double>
    val totalLend: StateFlow<Double>
    val totalBorrow: StateFlow<Double>

    val spendingCategoryTotals: StateFlow<List<CategoryTotal>>
    val earningCategoryTotals: StateFlow<List<CategoryTotal>>
    val debtCategoryTotals: StateFlow<List<CategoryTotal>>
    val debtByPerson: StateFlow<List<DebtPersonTotal>>

    val currencySymbol: StateFlow<String>
    val isDarkMode: StateFlow<Boolean>

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
    val selectedMonth: StateFlow<Calendar> = _selectedMonth.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(database.expenseDao(), database.categoryDao())
        preferencesManager = PreferencesManager.getInstance(application)

        currencySymbol = preferencesManager.currencySymbol
        isDarkMode = preferencesManager.isDarkMode

        // Ensure default categories exist
        viewModelScope.launch {
            ensureDefaultCategories()
        }

        spendingCategories = repository.getCategoriesByType("SPENDING")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        earningCategories = repository.getCategoriesByType("EARNING")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        debtCategories = repository.getCategoriesByType("DEBT")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allCategories = repository.allCategories
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        currentMonthSpending = _selectedMonth.flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getExpensesByTypeAndDateRange(TransactionType.SPENDING.name, start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        currentMonthEarnings = _selectedMonth.flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getExpensesByTypeAndDateRange(TransactionType.EARNING.name, start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        currentMonthDebt = _selectedMonth.flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getExpensesByTypeAndDateRange(TransactionType.DEBT.name, start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        totalSpending = _selectedMonth.flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getTotalSpendingByDateRange(start, end)
        }.map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        totalEarnings = _selectedMonth.flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getTotalEarningsByDateRange(start, end)
        }.map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        totalDebt = _selectedMonth.flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getTotalDebtByDateRange(start, end)
        }.map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        totalLend = _selectedMonth.flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getTotalLendByDateRange(start, end)
        }.map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        totalBorrow = _selectedMonth.flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getTotalBorrowByDateRange(start, end)
        }.map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        spendingCategoryTotals = _selectedMonth.flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getSpendingCategoryTotals(start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        earningCategoryTotals = _selectedMonth.flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getEarningCategoryTotals(start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        debtCategoryTotals = _selectedMonth.flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getDebtCategoryTotals(start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        debtByPerson = _selectedMonth.flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getDebtByPerson(start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addExpense(
        amount: Double,
        category: String,
        date: Long,
        note: String,
        type: TransactionType,
        debtType: String? = null,
        personName: String? = null
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    amount = amount,
                    category = category,
                    date = date,
                    note = note,
                    type = type.name,
                    debtType = debtType,
                    personName = personName
                )
            )
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    suspend fun getExpenseById(id: Long): Expense? = repository.getExpenseById(id)

    fun addCategory(name: String, categoryType: String) {
        viewModelScope.launch {
            repository.insertCategory(Category(name = name, isDefault = false, categoryType = categoryType))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun setSelectedMonth(year: Int, month: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
        }
        _selectedMonth.value = calendar
    }

    fun previousMonth() {
        val calendar = _selectedMonth.value.clone() as Calendar
        calendar.add(Calendar.MONTH, -1)
        _selectedMonth.value = calendar
    }

    fun nextMonth() {
        val calendar = _selectedMonth.value.clone() as Calendar
        calendar.add(Calendar.MONTH, 1)
        _selectedMonth.value = calendar
    }

    fun setCurrency(code: String, symbol: String) {
        preferencesManager.setCurrency(code, symbol)
    }

    fun setDarkMode(enabled: Boolean) {
        preferencesManager.setDarkMode(enabled)
    }

    private fun getMonthRange(calendar: Calendar): Pair<Long, Long> {
        val start = calendar.clone() as Calendar
        start.set(Calendar.DAY_OF_MONTH, 1)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)

        val end = calendar.clone() as Calendar
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))
        end.set(Calendar.HOUR_OF_DAY, 23)
        end.set(Calendar.MINUTE, 59)
        end.set(Calendar.SECOND, 59)
        end.set(Calendar.MILLISECOND, 999)

        return Pair(start.timeInMillis, end.timeInMillis)
    }

    fun getAllExpensesForExport(): Flow<List<Expense>> = repository.allExpenses

    private suspend fun ensureDefaultCategories() {
        val count = repository.getCategoryCount()
        if (count == 0) {
            // Add spending categories
            val spendingList = listOf("Food", "Grocery", "Bills", "Travel", "Shopping", "Entertainment", "Health", "Family", "Education", "Other")
            spendingList.forEach { name ->
                repository.insertCategory(Category(name = name, isDefault = true, categoryType = "SPENDING"))
            }
            // Add earning categories
            val earningList = listOf("Salary", "Business", "Investment", "Interest", "Extra Income", "Other")
            earningList.forEach { name ->
                repository.insertCategory(Category(name = name, isDefault = true, categoryType = "EARNING"))
            }
            // Add debt categories
            val debtList = listOf("Lend", "Borrow")
            debtList.forEach { name ->
                repository.insertCategory(Category(name = name, isDefault = true, categoryType = "DEBT"))
            }
        }
    }
}
