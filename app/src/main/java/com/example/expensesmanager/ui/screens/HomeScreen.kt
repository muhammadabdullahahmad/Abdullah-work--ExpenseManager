package com.example.expensesmanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensesmanager.data.model.Expense
import com.example.expensesmanager.data.model.TransactionType
import com.example.expensesmanager.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    onAddExpense: (String) -> Unit,
    onEditExpense: (Long) -> Unit
) {
    val spending by viewModel.currentMonthSpending.collectAsState()
    val earnings by viewModel.currentMonthEarnings.collectAsState()
    val debt by viewModel.currentMonthDebt.collectAsState()

    val totalSpending by viewModel.totalSpending.collectAsState()
    val totalEarnings by viewModel.totalEarnings.collectAsState()
    val totalDebt by viewModel.totalDebt.collectAsState()

    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Spending", "Earnings", "Debt")

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val balance = totalEarnings - totalSpending - totalDebt

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val type = when (selectedTab) {
                        0 -> TransactionType.SPENDING.name
                        1 -> TransactionType.EARNING.name
                        else -> TransactionType.DEBT.name
                    }
                    onAddExpense(type)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month selector
            MonthSelector(
                currentMonth = monthFormat.format(selectedMonth.time),
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )

            // Balance summary card
            BalanceSummaryCard(
                earnings = totalEarnings,
                spending = totalSpending,
                debt = totalDebt,
                balance = balance,
                currencySymbol = currencySymbol
            )

            // Tabs for Spending, Earnings, Debt
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Content based on selected tab
            val currentList = when (selectedTab) {
                0 -> spending
                1 -> earnings
                else -> debt
            }

            val total = when (selectedTab) {
                0 -> totalSpending
                1 -> totalEarnings
                else -> totalDebt
            }

            // Total for current tab
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (selectedTab) {
                        0 -> Color(0xFFFFEBEE) // Red tint for spending
                        1 -> Color(0xFFE8F5E9) // Green tint for earnings
                        else -> Color(0xFFFFF3E0) // Orange tint for debt
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total ${tabs[selectedTab]}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$currencySymbol%.2f".format(total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when (selectedTab) {
                            0 -> Color(0xFFC62828)
                            1 -> Color(0xFF2E7D32)
                            else -> Color(0xFFE65100)
                        }
                    )
                }
            }

            // Transaction list
            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ${tabs[selectedTab].lowercase()} this month",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentList) { expense ->
                        ExpenseItem(
                            expense = expense,
                            currencySymbol = currencySymbol,
                            onClick = { onEditExpense(expense.id) },
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    currentMonth: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPreviousMonth) {
            Text("<", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            text = currentMonth,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onNextMonth) {
            Text(">", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun BalanceSummaryCard(
    earnings: Double,
    spending: Double,
    debt: Double,
    balance: Double,
    currencySymbol: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Balance",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "$currencySymbol%.2f".format(balance),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (balance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Earnings",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$currencySymbol%.2f".format(earnings),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Spending",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$currencySymbol%.2f".format(spending),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC62828)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Debt",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$currencySymbol%.2f".format(debt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    currencySymbol: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                // Show person name for debt transactions
                if (expense.type == TransactionType.DEBT.name && !expense.personName.isNullOrEmpty()) {
                    Text(
                        text = "${if (expense.category == "Lend") "Lent to" else "Borrowed from"}: ${expense.personName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (expense.note.isNotEmpty()) {
                    Text(
                        text = expense.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = dateFormat.format(Date(expense.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$currencySymbol%.2f".format(expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when (expense.type) {
                    TransactionType.EARNING.name -> Color(0xFF2E7D32)
                    TransactionType.DEBT.name -> Color(0xFFE65100)
                    else -> Color(0xFFC62828)
                }
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
