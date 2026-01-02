package com.example.expensesmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false,
    val categoryType: String = "SPENDING" // SPENDING, EARNING, or DEBT
)

object DefaultCategories {
    val spendingList = listOf(
        "Food",
        "Grocery",
        "Bills",
        "Travel",
        "Shopping",
        "Entertainment",
        "Health",
        "Family",
        "Education",
        "Other"
    )

    val earningList = listOf(
        "Salary",
        "Business",
        "Investment",
        "Interest",
        "Extra Income",
        "Other"
    )

    val debtList = listOf(
        "Lend",
        "Borrow"
    )
}
