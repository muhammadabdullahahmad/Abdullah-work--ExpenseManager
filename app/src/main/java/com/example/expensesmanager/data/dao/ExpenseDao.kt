package com.example.expensesmanager.data.dao

import androidx.room.*
import com.example.expensesmanager.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE type = :type AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByTypeAndDateRange(type: String, startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'SPENDING' AND date BETWEEN :startDate AND :endDate")
    fun getTotalSpendingByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'EARNING' AND date BETWEEN :startDate AND :endDate")
    fun getTotalEarningsByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'DEBT' AND date BETWEEN :startDate AND :endDate")
    fun getTotalDebtByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE type = 'SPENDING' AND date BETWEEN :startDate AND :endDate GROUP BY category")
    fun getSpendingCategoryTotals(startDate: Long, endDate: Long): Flow<List<CategoryTotal>>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE type = 'EARNING' AND date BETWEEN :startDate AND :endDate GROUP BY category")
    fun getEarningCategoryTotals(startDate: Long, endDate: Long): Flow<List<CategoryTotal>>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE type = 'DEBT' AND date BETWEEN :startDate AND :endDate GROUP BY category")
    fun getDebtCategoryTotals(startDate: Long, endDate: Long): Flow<List<CategoryTotal>>

    @Query("SELECT category, personName, SUM(amount) as total FROM expenses WHERE type = 'DEBT' AND date BETWEEN :startDate AND :endDate GROUP BY category, personName")
    fun getDebtByPerson(startDate: Long, endDate: Long): Flow<List<DebtPersonTotal>>

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}

data class CategoryTotal(
    val category: String,
    val total: Double
)

data class DebtPersonTotal(
    val category: String,
    val personName: String?,
    val total: Double
)
