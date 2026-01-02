package com.example.expensesmanager.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensesmanager.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// Chart colors
private val chartColors = listOf(
    Color(0xFF2196F3),
    Color(0xFF4CAF50),
    Color(0xFFFFC107),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFFFF5722),
    Color(0xFF00BCD4),
    Color(0xFF795548),
    Color(0xFF607D8B),
    Color(0xFF8BC34A)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: ExpenseViewModel
) {
    val spendingCategoryTotals by viewModel.spendingCategoryTotals.collectAsState()
    val earningCategoryTotals by viewModel.earningCategoryTotals.collectAsState()
    val debtCategoryTotals by viewModel.debtCategoryTotals.collectAsState()
    val debtByPerson by viewModel.debtByPerson.collectAsState()
    val totalSpending by viewModel.totalSpending.collectAsState()
    val totalEarnings by viewModel.totalEarnings.collectAsState()
    val totalDebt by viewModel.totalDebt.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Spending", "Earnings", "Debt")

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    val currentCategoryTotals = when (selectedTab) {
        0 -> spendingCategoryTotals
        1 -> earningCategoryTotals
        else -> debtCategoryTotals
    }
    val currentTotal = when (selectedTab) {
        0 -> totalSpending
        1 -> totalEarnings
        else -> totalDebt
    }

    // Filter non-zero categories and sort by amount
    val filteredCategories = currentCategoryTotals
        .filter { it.total > 0 }
        .sortedByDescending { it.total }

    // For debt tab - filter and sort by person
    val filteredDebtByPerson = debtByPerson
        .filter { it.total > 0 }
        .sortedByDescending { it.total }

    // Calculate percentages
    val categoryPercentages = filteredCategories.mapIndexed { index, cat ->
        val percentage = if (currentTotal > 0) (cat.total / currentTotal * 100.0) else 0.0
        Triple(cat.category, (percentage * 10).roundToInt() / 10.0, chartColors[index % chartColors.size])
    }

    // Calculate debt percentages with person names
    data class DebtDisplayItem(
        val category: String,
        val personName: String,
        val amount: Double,
        val percentage: Double,
        val color: Color
    )

    val debtPercentages = filteredDebtByPerson.mapIndexed { index, debt ->
        val percentage = if (totalDebt > 0) (debt.total / totalDebt * 100.0) else 0.0
        DebtDisplayItem(
            category = debt.category,
            personName = debt.personName ?: "Unknown",
            amount = debt.total,
            percentage = (percentage * 10).roundToInt() / 10.0,
            color = chartColors[index % chartColors.size]
        )
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month selector
            item {
                MonthSelector(
                    currentMonth = monthFormat.format(selectedMonth.time),
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
            }

            // Summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Monthly Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Earnings:")
                            Text(
                                "$currencySymbol%.2f".format(totalEarnings),
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Spending:")
                            Text(
                                "$currencySymbol%.2f".format(totalSpending),
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Debt:")
                            Text(
                                "$currencySymbol%.2f".format(totalDebt),
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Net Balance:", fontWeight = FontWeight.Bold)
                            val netBalance = totalEarnings - totalSpending - totalDebt
                            Text(
                                "$currencySymbol%.2f".format(netBalance),
                                color = if (netBalance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tab selector
            item {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }

            // Show pie chart with labels if data available
            if (selectedTab == 2) {
                // Debt tab - show with person names
                if (debtPercentages.isNotEmpty() && totalDebt > 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Debt by Person",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Pie chart
                                val debtChartData = debtPercentages.map {
                                    Triple("${it.category}: ${it.personName}", it.percentage, it.color)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(350.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PieChartWithLabels(
                                        categoryPercentages = debtChartData,
                                        currencySymbol = currencySymbol,
                                        total = totalDebt
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Legend with person names
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    debtPercentages.forEach { item ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = item.color.copy(alpha = 0.1f)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .clip(CircleShape)
                                                                .background(item.color)
                                                        )
                                                        Text(
                                                            text = item.category,
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Text(
                                                        text = "%.1f%%".format(item.percentage),
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = item.color
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Amount: $currencySymbol%.2f".format(item.amount),
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "Person: ${item.personName}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = item.color
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (filteredCategories.isNotEmpty() && currentTotal > 0) {
                // Spending and Earnings tabs
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${tabs[selectedTab]} by Category",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Pie chart with labels ON the chart
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                PieChartWithLabels(
                                    categoryPercentages = categoryPercentages,
                                    currencySymbol = currencySymbol,
                                    total = currentTotal
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Legend below chart
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categoryPercentages.forEach { (name, percentage, color) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Text(
                                                text = name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Text(
                                            text = "%.1f%%".format(percentage),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Empty state
            val isEmpty = if (selectedTab == 2) debtPercentages.isEmpty() else filteredCategories.isEmpty()
            if (isEmpty) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        )
                    ) {
                        Text(
                            text = "No ${tabs[selectedTab].lowercase()} data this month.\n\nAdd some transactions on Home screen first!",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PieChartWithLabels(
    categoryPercentages: List<Triple<String, Double, Color>>,
    currencySymbol: String,
    total: Double
) {
    Canvas(
        modifier = Modifier.size(300.dp)
    ) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2 * 0.6f
        val center = Offset(size.width / 2, size.height / 2)
        val strokeWidth = 60f

        var startAngle = -90f

        categoryPercentages.forEach { (name, percentage, color) ->
            val sweepAngle = (percentage / 100.0 * 360.0).toFloat()

            // Draw arc segment
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )

            // Calculate label position (on the arc)
            val midAngle = startAngle + sweepAngle / 2
            val labelRadius = radius + strokeWidth / 2 + 50f
            val labelX = center.x + labelRadius * cos(Math.toRadians(midAngle.toDouble())).toFloat()
            val labelY = center.y + labelRadius * sin(Math.toRadians(midAngle.toDouble())).toFloat()

            // Draw label text
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    textSize = 32f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    this.color = android.graphics.Color.BLACK
                }

                // Draw category name
                drawText(
                    name,
                    labelX,
                    labelY - 10,
                    paint
                )

                // Draw percentage
                paint.color = color.hashCode()
                drawText(
                    "%.1f%%".format(percentage),
                    labelX,
                    labelY + 25,
                    paint
                )
            }

            startAngle += sweepAngle
        }

        // Draw center circle (white background)
        drawCircle(
            color = Color.White,
            radius = radius - strokeWidth / 2 - 10f,
            center = center
        )

        // Draw total in center
        drawContext.canvas.nativeCanvas.apply {
            val paintLabel = android.graphics.Paint().apply {
                textSize = 28f
                textAlign = android.graphics.Paint.Align.CENTER
                color = android.graphics.Color.GRAY
            }
            drawText("Total", center.x, center.y - 15, paintLabel)

            val paintValue = android.graphics.Paint().apply {
                textSize = 36f
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
                color = android.graphics.Color.BLACK
            }
            drawText("$currencySymbol%.2f".format(total), center.x, center.y + 25, paintValue)
        }
    }
}
