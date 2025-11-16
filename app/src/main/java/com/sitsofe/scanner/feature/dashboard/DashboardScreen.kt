package com.sitsofe.scanner.feature.dashboard

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sitsofe.scanner.ui.PreviewMocks
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    vm: DashboardViewModel,
    outerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val loading by vm.loading.collectAsState()
    val sum by vm.summary.collectAsState()
    val series by vm.series.collectAsState()
    val filter by vm.filter.collectAsState()

    LaunchedEffect(Unit) { vm.initLoad() }

    val ctx = LocalContext.current
    val snack = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snack) }) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(outerPadding)
                .padding(inner)
                .navigationBarsPadding()
        ) {
            // Header row
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Export button (green) – Surface + IconButton for solid background
                    Surface(
                        color = Color(0xFF16A34A),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        IconButton(onClick = {
                            val text = vm.exportText()
                            shareText(ctx, "dashboard.csv", text)
                        }) {
                            Icon(Icons.Outlined.Download, contentDescription = "Export")
                        }
                    }

                    // Date filter (blue)
                    var showDateDialog by remember { mutableStateOf(false) }
                    Button(
                        onClick = { showDateDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Outlined.FilterList, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Filter date")
                    }
                    if (showDateDialog) {
                        DateRangeDialog(
                            onDismiss = { showDateDialog = false },
                            onApply = { s, e ->
                                showDateDialog = false
                                vm.setDateRange(s, e)
                            }
                        )
                    }
                }
            }

            if (loading && sum == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sales performance chart card
                item {
                    DashboardCard(
                        title = "Sales Performance",
                        right = {
                            QuickFilterCompact(
                                value = filter,
                                onChange = { vm.setQuickFilter(it) }
                            )
                        }
                    ) {
                        val sales = series?.salesData?.map { it.totalSales } ?: emptyList()
                        val tx = series?.salesData?.map { it.totalTransactions.toDouble() } ?: emptyList()
                        val labels = series?.salesData?.map { it.date.takeLast(2) } ?: emptyList()
                        LineChart(
                            sales = sales,
                            txns = tx,
                            labels = labels,
                            salesColor = Color(0xFFF59E0B),
                            txnsColor = Color(0xFF10B981),
                            height = 200.dp
                        )
                        Spacer(Modifier.height(8.dp))
                        LegendRow(
                            listOf(
                                "Total Sales (LRD)" to Color(0xFFF59E0B),
                                "Total Transactions" to Color(0xFF10B981)
                            )
                        )
                    }
                }

                // Sales Performance summary
                item {
                    SectionCard("Sales Performance") {
                        MetricRow("Total Sales", sum?.totalSales, accent = Color(0xFF16A34A))
                        MetricRow("Cash Sales", sum?.totalCashSales)
                        MetricRow("Internal Sales", sum?.internalSalesCost)
                    }
                }

                // Inventory & Stock
                item {
                    SectionCard("Inventory & Stock") {
                        MetricRow("Purchase Amount", sum?.totalPurchaseAmount)
                        MetricRow("Current Stock Value", sum?.currentStockValue, bold = true)
                        MetricRow("Cost Price", sum?.totalCostPrice)
                        MetricRow("Total Stock", sum?.totalStock)
                        MetricRow("Out of Stock Items", sum?.outOfStockItems?.toDouble())
                        MetricRow("Expiring Products", sum?.expiringProducts?.toDouble())
                        MetricRow("Expired Products", sum?.expiredProducts?.toDouble(), dangerIfPositive = true)
                    }
                }

                // Sales By Category
                item {
                    SectionCard("Sales By Category") {
                        sum?.salesByCategory.orEmpty().forEachIndexed { idx, it ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${idx + 1}   ${it.categoryName}")
                                Text("LRD ${formatMoney(it.totalSales)}", fontWeight = FontWeight.SemiBold)
                            }
                            Divider()
                        }
                    }
                }

                // Customers
                item {
                    SectionCard("Customers") {
                        MetricRow("Total Customers", sum?.totalCustomers?.toDouble())
                        MetricRow("Qualified Customers", sum?.qualifiedCustomers?.toDouble())
                    }
                }

                // Profitability
                item {
                    SectionCard("Profitability") {
                        MetricRow("Cost of Sales", sum?.totalCostOfSales)
                        MetricRow("Gross Profit", sum?.grossProfit, accent = Color(0xFFF59E0B))
                        MetricRow("Expenses", sum?.expensesTotal)
                        MetricRow("Net Profit", sum?.netProfit, dangerIfNegative = true)
                        MetricRow("Profit Margin", sum?.profitMargin, suffix = "%")
                    }
                }

                // Top / Low selling
                item {
                    SectionCard("Top Selling Products") {
                        sum?.topSellingProducts.orEmpty().forEachIndexed { idx, it ->
                            RankRow(idx + 1, it.productName, it.totalSold)
                            Divider()
                        }
                    }
                }
                item {
                    SectionCard("Low Selling Products") {
                        sum?.lowSellingProducts.orEmpty().forEachIndexed { idx, it ->
                            RankRow(idx + 1, it.productName, it.totalSold)
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

/* ===== UI helpers ===== */

@Composable
private fun DashboardCard(
    title: String,
    right: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                right?.invoke()
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: Double?,
    suffix: String = "",
    accent: Color? = null,
    bold: Boolean = false,
    dangerIfPositive: Boolean = false,
    dangerIfNegative: Boolean = false
) {
    val v = value ?: 0.0
    val danger = (dangerIfPositive && v > 0) || (dangerIfNegative && v < 0)
    val color = when {
        danger -> Color(0xFFE11D48)
        accent != null -> accent
        else -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Text(
            text = if (suffix == "%") "${formatMoney(v)}$suffix" else "LRD ${formatMoney(v)}$suffix",
            color = color,
            fontWeight = if (bold || accent != null) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun RankRow(pos: Int, name: String, qty: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$pos   $name")
        Text(qty.toString(), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LegendRow(items: List<Pair<String, Color>>) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items.forEach { (label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(color, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun formatMoney(v: Double): String = "%,.2f".format(v)

/* ===== Quick filter (stable API only) ===== */

@Composable
private fun QuickFilterCompact(value: String, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Button(
            onClick = { open = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(value)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
        }
        androidx.compose.material3.DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false }
        ) {
            listOf("last7days", "last30days", "thisMonth").forEach { opt ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = { onChange(opt); open = false }
                )
            }
        }
    }
}

/* ===== Date range dialog (no experimental APIs) ===== */

@Composable
private fun DateRangeDialog(
    onDismiss: () -> Unit,
    onApply: (LocalDate, LocalDate) -> Unit
) {
    val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var startText by remember { mutableStateOf(LocalDate.now().minusDays(6).format(df)) }
    var endText by remember { mutableStateOf(LocalDate.now().format(df)) }
    var error by remember { mutableStateOf<String?>(null) }

    fun tryApply() {
        error = null
        val s = runCatching { LocalDate.parse(startText.trim(), df) }.getOrNull()
        val e = runCatching { LocalDate.parse(endText.trim(), df) }.getOrNull()
        when {
            s == null || e == null -> error = "Use format: yyyy-MM-dd"
            e.isBefore(s) -> error = "End date must be after start date"
            else -> onApply(s, e)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { tryApply() }) { Text("Apply") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Filter by date range") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = startText,
                    onValueChange = { startText = it },
                    singleLine = true,
                    label = { Text("Start (yyyy-MM-dd)") },
                    placeholder = { Text("2025-10-05") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endText,
                    onValueChange = { endText = it },
                    singleLine = true,
                    label = { Text("End (yyyy-MM-dd)") },
                    placeholder = { Text("2025-10-11") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(error!!, color = Color(0xFFE11D48), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    )
}

/* ===== Simple dual-line chart (Canvas) ===== */

@Composable
private fun LineChart(
    sales: List<Double>,
    txns: List<Double>,
    labels: List<String>,
    salesColor: Color,
    txnsColor: Color,
    height: Dp
) {
    val count = maxOf(sales.size, txns.size)
    if (count == 0) {
        Box(Modifier.fillMaxWidth().height(height), contentAlignment = Alignment.Center) {
            Text("No data")
        }
        return
    }
    val padH = 16.dp
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = padH)
    ) {
        val w = size.width
        val h = size.height
        val stepX = if (count <= 1) w else w / (count - 1)

        val maxSales = (sales.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
        val maxTx = (txns.maxOrNull() ?: 0.0).coerceAtLeast(1.0)

        val pSales = Path()
        val pTx = Path()
        for (i in 0 until count) {
            val x = i * stepX
            val ySales = h - ((sales.getOrNull(i) ?: 0.0) / maxSales).toFloat() * (h * 0.9f)
            val yTx = h - ((txns.getOrNull(i) ?: 0.0) / maxTx).toFloat() * (h * 0.9f)
            if (i == 0) {
                pSales.moveTo(x, ySales); pTx.moveTo(x, yTx)
            } else {
                pSales.lineTo(x, ySales); pTx.lineTo(x, yTx)
            }
        }
        drawPath(
            path = pSales,
            color = salesColor,
            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = pTx,
            color = txnsColor,
            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/* ===== Share helper ===== */

private fun shareText(ctx: android.content.Context, filename: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, filename)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(Intent.createChooser(intent, "Export dashboard"))
}

@Preview(showBackground = true, widthDp = 400, heightDp = 1200)
@Composable
private fun DashboardScreenPreview() {
    DashboardScreen(vm = PreviewMocks.dashboardViewModel)
}
