package com.example.expensesmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    SPENDING,
    EARNING,
    DEBT
}

enum class DebtType {
    LEND,
    BORROW
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val date: Long, // timestamp in milliseconds
    val note: String = "",
    val type: String = TransactionType.SPENDING.name, // SPENDING, EARNING, or DEBT
    val debtType: String? = null, // LEND or BORROW (only for DEBT type)
    val personName: String? = null // Lender or Borrower name
)
