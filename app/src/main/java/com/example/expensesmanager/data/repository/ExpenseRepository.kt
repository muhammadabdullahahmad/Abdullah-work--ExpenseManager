package com.example.expensesmanager.data.repository

import com.example.expensesmanager.data.dao.CategoryTotal
import com.example.expensesmanager.data.dao.DebtPersonTotal
import com.example.expensesmanager.data.dao.ExpenseDao
import com.example.expensesmanager.data.dao.CategoryDao
import com.example.expensesmanager.data.model.Category
import com.example.expensesmanager.data.model.Expense
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
) {
    // Expense operations
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun insertExpense(expense: Expense): Long = expenseDao.insert(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)

    suspend fun getExpenseById(id: Long): Expense? = expenseDao.getExpenseById(id)

    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> =
        expenseDao.getExpensesByDateRange(startDate, endDate)

    fun getExpensesByTypeAndDateRange(type: String, startDate: Long, endDate: Long): Flow<List<Expense>> =
        expenseDao.getExpensesByTypeAndDateRange(type, startDate, endDate)

    fun getExpensesByCategory(category: String): Flow<List<Expense>> =
        expenseDao.getExpensesByCategory(category)

    fun getTotalSpendingByDateRange(startDate: Long, endDate: Long): Flow<Double?> =
        expenseDao.getTotalSpendingByDateRange(startDate, endDate)

    fun getTotalEarningsByDateRange(startDate: Long, endDate: Long): Flow<Double?> =
        expenseDao.getTotalEarningsByDateRange(startDate, endDate)

    fun getTotalDebtByDateRange(startDate: Long, endDate: Long): Flow<Double?> =
        expenseDao.getTotalDebtByDateRange(startDate, endDate)

    fun getTotalLendByDateRange(startDate: Long, endDate: Long): Flow<Double?> =
        expenseDao.getTotalLendByDateRange(startDate, endDate)

    fun getTotalBorrowByDateRange(startDate: Long, endDate: Long): Flow<Double?> =
        expenseDao.getTotalBorrowByDateRange(startDate, endDate)

    fun getSpendingCategoryTotals(startDate: Long, endDate: Long): Flow<List<CategoryTotal>> =
        expenseDao.getSpendingCategoryTotals(startDate, endDate)

    fun getEarningCategoryTotals(startDate: Long, endDate: Long): Flow<List<CategoryTotal>> =
        expenseDao.getEarningCategoryTotals(startDate, endDate)

    fun getDebtCategoryTotals(startDate: Long, endDate: Long): Flow<List<CategoryTotal>> =
        expenseDao.getDebtCategoryTotals(startDate, endDate)

    fun getDebtByPerson(startDate: Long, endDate: Long): Flow<List<DebtPersonTotal>> =
        expenseDao.getDebtByPerson(startDate, endDate)

    suspend fun deleteAllExpenses() = expenseDao.deleteAll()

    // Category operations
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    fun getCategoriesByType(type: String): Flow<List<Category>> = categoryDao.getCategoriesByType(type)

    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    suspend fun getCategoryCount(): Int = categoryDao.getCategoryCount()
}
