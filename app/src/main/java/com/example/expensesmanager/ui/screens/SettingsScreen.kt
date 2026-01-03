package com.example.expensesmanager.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.expensesmanager.data.model.Currency
import com.example.expensesmanager.data.model.CurrencyList
import com.example.expensesmanager.ui.viewmodel.ExpenseViewModel
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ExpenseViewModel,
    onChangePin: () -> Unit = {}
) {
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showCurrencyDialog by remember { mutableStateOf(false) }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance section
            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Dark Mode",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (isDarkMode) "Enabled" else "Disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.setDarkMode(it) }
                        )
                    }
                }
            }

            // Security section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Security",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChangePin() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Change PIN",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Update your security PIN",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Change",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Currency section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Currency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCurrencyDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Change Currency",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Current: $currencySymbol",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Change",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Backup section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Data Backup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            exportData(context, viewModel)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export Data to JSON")
                }
            }

            item {
                Text(
                    text = "Exports all expenses to a JSON file that you can share or save.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Currency selection dialog
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentSymbol = currencySymbol,
            onDismiss = { showCurrencyDialog = false },
            onCurrencySelected = { currency ->
                viewModel.setCurrency(currency.code, currency.symbol)
                showCurrencyDialog = false
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionDialog(
    currentSymbol: String,
    onDismiss: () -> Unit,
    onCurrencySelected: (Currency) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCurrencies = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            CurrencyList.currencies
        } else {
            CurrencyList.currencies.filter { currency ->
                currency.name.contains(searchQuery, ignoreCase = true) ||
                        currency.code.contains(searchQuery, ignoreCase = true) ||
                        currency.symbol.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search currency") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredCurrencies) { currency ->
                        val isSelected = currency.symbol == currentSymbol
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCurrencySelected(currency) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currency.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${currency.code} (${currency.symbol})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private suspend fun exportData(context: Context, viewModel: ExpenseViewModel) {
    try {
        val expenses = viewModel.getAllExpensesForExport().first()

        if (expenses.isEmpty()) {
            Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(expenses)

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "expenses_backup_${dateFormat.format(Date())}.json"

        val file = File(context.cacheDir, fileName)
        file.writeText(json)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Export Expenses"))
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
