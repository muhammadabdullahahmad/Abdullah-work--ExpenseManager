package com.example.expensesmanager.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensesmanager.data.model.DebtType
import com.example.expensesmanager.data.model.Expense
import com.example.expensesmanager.data.model.TransactionType
import com.example.expensesmanager.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    transactionType: String = TransactionType.SPENDING.name,
    expenseId: Long? = null,
    onNavigateBack: () -> Unit
) {
    val spendingCategories by viewModel.spendingCategories.collectAsState()
    val earningCategories by viewModel.earningCategories.collectAsState()
    val debtCategories by viewModel.debtCategories.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val context = LocalContext.current

    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var note by remember { mutableStateOf("") }
    var currentType by remember { mutableStateOf(transactionType) }
    var expanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var existingExpense by remember { mutableStateOf<Expense?>(null) }
    var selectedDebtType by remember { mutableStateOf(DebtType.LEND.name) }
    var personName by remember { mutableStateOf("") }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Get appropriate categories based on type
    val displayCategories = when (currentType) {
        TransactionType.SPENDING.name -> spendingCategories
        TransactionType.EARNING.name -> earningCategories
        TransactionType.DEBT.name -> debtCategories
        else -> spendingCategories
    }

    // Load existing expense if editing
    LaunchedEffect(expenseId) {
        if (expenseId != null && expenseId > 0) {
            viewModel.getExpenseById(expenseId)?.let { expense ->
                existingExpense = expense
                amount = expense.amount.toString()
                selectedCategory = expense.category
                selectedDate = expense.date
                note = expense.note
                currentType = expense.type
                isEditing = true
                expense.debtType?.let { selectedDebtType = it }
                expense.personName?.let { personName = it }
            }
        }
    }

    // Set default category when categories load
    LaunchedEffect(displayCategories, currentType) {
        if (selectedCategory.isEmpty() && displayCategories.isNotEmpty()) {
            selectedCategory = displayCategories.first().name
        }
    }

    val title = when {
        isEditing -> "Edit Entry"
        currentType == TransactionType.SPENDING.name -> "Add Spending"
        currentType == TransactionType.EARNING.name -> "Add Earning"
        else -> "Add Debt"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type selector (only show when not editing)
            if (!isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentType == TransactionType.SPENDING.name,
                        onClick = {
                            currentType = TransactionType.SPENDING.name
                            selectedCategory = ""
                        },
                        label = { Text("Spending") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = currentType == TransactionType.EARNING.name,
                        onClick = {
                            currentType = TransactionType.EARNING.name
                            selectedCategory = ""
                        },
                        label = { Text("Earning") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = currentType == TransactionType.DEBT.name,
                        onClick = {
                            currentType = TransactionType.DEBT.name
                            selectedCategory = ""
                        },
                        label = { Text("Debt") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Amount input
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amount = newValue
                    }
                },
                label = { Text("Amount ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = if (selectedCategory.isEmpty()) "Select Category" else selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (displayCategories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No categories available") },
                            onClick = { expanded = false }
                        )
                    } else {
                        displayCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Date picker
            OutlinedTextField(
                value = dateFormat.format(Date(selectedDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val currentCalendar = Calendar.getInstance().apply {
                                    timeInMillis = selectedDate
                                }
                                currentCalendar.set(year, month, dayOfMonth)
                                selectedDate = currentCalendar.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Time picker
            OutlinedTextField(
                value = timeFormat.format(Date(selectedDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Time") },
                trailingIcon = {
                    IconButton(onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                        }
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val currentCalendar = Calendar.getInstance().apply {
                                    timeInMillis = selectedDate
                                }
                                currentCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                currentCalendar.set(Calendar.MINUTE, minute)
                                selectedDate = currentCalendar.timeInMillis
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select time")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Lender/Borrower name for Debt based on category
            if (currentType == TransactionType.DEBT.name && selectedCategory.isNotEmpty()) {
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = {
                        Text(
                            if (selectedCategory == "Lend") "Lender Name (Who you lent to)"
                            else "Borrower Name (Who you borrowed from)"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Note input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                supportingText = { Text("Add a short description") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0 && selectedCategory.isNotEmpty()) {
                        if (isEditing && existingExpense != null) {
                            viewModel.updateExpense(
                                existingExpense!!.copy(
                                    amount = amountValue,
                                    category = selectedCategory,
                                    date = selectedDate,
                                    note = note,
                                    type = currentType,
                                    debtType = if (currentType == TransactionType.DEBT.name) selectedDebtType else null,
                                    personName = if (currentType == TransactionType.DEBT.name) personName else null
                                )
                            )
                        } else {
                            viewModel.addExpense(
                                amount = amountValue,
                                category = selectedCategory,
                                date = selectedDate,
                                note = note,
                                type = TransactionType.valueOf(currentType),
                                debtType = if (currentType == TransactionType.DEBT.name) selectedDebtType else null,
                                personName = if (currentType == TransactionType.DEBT.name) personName else null
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0 && selectedCategory.isNotEmpty()
            ) {
                Text(if (isEditing) "Update" else "Save")
            }
        }
    }
}
