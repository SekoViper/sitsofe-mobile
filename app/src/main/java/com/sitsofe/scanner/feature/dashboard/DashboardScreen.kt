package com.sitsofe.scanner.feature.dashboard

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sitsofe.scanner.core.network.DashboardSeriesDto
import com.sitsofe.scanner.core.network.DashboardSummaryDto
import com.sitsofe.scanner.core.network.RankedProductDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.roundToInt

object PharmacyColors {
    val Primary = Color(0xFF4A90E2)
    val Secondary = Color(0xFF50C8C8)
    val LightCyan = Color(0xFFB3E5E5)
    val DarkNavy = Color(0xFF1A3A52)
    val BackgroundGray = Color(0xFFF5F7FA)
    val CardWhite = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF2C3E50)
    val TextSecondary = Color(0xFF7F8C8D)
    val Success = Color(0xFF27AE60)
    val Warning = Color(0xFFF39C12)
    val Danger = Color(0xFFE74C3C)
    val ChartLine1 = Color(0xFF2C3E50)
    val ChartLine2 = Color(0xFF50C8C8)
    val ChartArea = Color(0x4050C8C8)
}

@OptIn(ExperimentalMaterial3Api::class)
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

    var showDateDialog by remember { mutableStateOf(false) }
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFB3D9E8),
            Color(0xFF9BCFDF),
            Color(0xFF8BC9D9)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snack) },
            containerColor = Color.Transparent,
            topBar = {
                DashboardTopBar(
                    onBack = { /* TODO hook navigation */ },
                    onChat = { /* future action */ },
                    onMenu = { /* future action */ },
                    onExport = {
                        val text = vm.exportText()
                        shareText(ctx, "dashboard.csv", text)
                    },
                    onFilterClick = { showDateDialog = true }
                )
            }
        ) { inner ->
            if (showDateDialog) {
                DateRangeDialog(
                    onDismiss = { showDateDialog = false },
                    onApply = { s, e ->
                        showDateDialog = false
                        vm.setDateRange(s, e)
                    }
                )
            }

            val bottomInset = inner.calculateBottomPadding() + outerPadding.calculateBottomPadding()

            if (loading && sum == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(outerPadding)
                        .padding(inner),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        FilterPillsRow(
                            filter = filter,
                            filterLabel = quickFilterLabel(filter),
                            onQuickFilterChange = { vm.setQuickFilter(it) },
                            onDateRangeClick = { showDateDialog = true }
                        )
                    }
                    item { ProfitCardsRow(sum) }
                    item { TransactionCard(series, quickFilterLabel(filter)) }
                    item { MetricsSection(sum) }
                    sum?.let { summary ->
                        item { EpicSellingProductsCard(summary.topSellingProducts, summary.lowSellingProducts) }
                        item { SummaryTableCard(summary) }
                    }
                    item { Spacer(Modifier.height(16.dp + bottomInset)) }
                }
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

/* ===== Dashboard chrome and custom cards ===== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    onBack: () -> Unit,
    onChat: () -> Unit,
    onMenu: () -> Unit,
    onExport: () -> Unit,
    onFilterClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Prowacams",
                fontWeight = FontWeight.Bold,
                color = PharmacyColors.TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PharmacyColors.TextPrimary)
            }
        },
        actions = {
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Outlined.FilterList, contentDescription = "Filter", tint = PharmacyColors.TextPrimary)
            }
            IconButton(onClick = onChat) {
                Icon(Icons.Filled.Chat, contentDescription = "Chat", tint = PharmacyColors.TextPrimary)
            }
            IconButton(onClick = onExport) {
                Icon(Icons.Outlined.Download, contentDescription = "Export", tint = PharmacyColors.TextPrimary)
            }
            IconButton(onClick = onMenu) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Menu", tint = PharmacyColors.TextPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PharmacyColors.CardWhite.copy(alpha = 0.95f),
            titleContentColor = PharmacyColors.TextPrimary
        )
    )
}

@Composable
private fun FilterPillsRow(
    filter: String,
    filterLabel: String,
    onQuickFilterChange: (String) -> Unit,
    onDateRangeClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Dashboard", color = PharmacyColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Showing $filterLabel", color = PharmacyColors.TextSecondary, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickFilterChip(value = filter, onChange = onQuickFilterChange)
            Button(
                onClick = onDateRangeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PharmacyColors.Secondary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Outlined.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Date range")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickFilterChip(value: String, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Surface(
            shape = RoundedCornerShape(50),
            color = PharmacyColors.CardWhite.copy(alpha = 0.9f),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .clickable { open = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(quickFilterLabel(value), color = PharmacyColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = PharmacyColors.TextPrimary)
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            listOf("last7days", "last30days", "thisMonth").forEach {
                DropdownMenuItem(
                    text = { Text(quickFilterLabel(it)) },
                    onClick = {
                        onChange(it)
                        open = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfitCardsRow(summary: DashboardSummaryDto?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfitCard(
            title = "Gross Profit L ↓",
            value = summary?.grossProfit?.let { "LRD ${formatMoney(it)}" } ?: "--",
            subtitle = "Gross Profit, LRD Qualified",
            backgroundColor = PharmacyColors.LightCyan,
            modifier = Modifier.weight(1f)
        )
        ProfitCard(
            title = "Net Profit ↓↓",
            value = summary?.netProfit?.let { "LRD ${formatMoney(it)}" } ?: "--",
            subtitle = "LRD Profit, LRD U/LL4/4.00",
            backgroundColor = Color(0xFFB8D4E6),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfitCard(
    title: String,
    value: String,
    subtitle: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 12.sp, color = PharmacyColors.CardWhite, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PharmacyColors.CardWhite)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 10.sp, color = PharmacyColors.CardWhite, lineHeight = 12.sp)
        }
    }
}

@Composable
private fun TransactionCard(series: DashboardSeriesDto?, filterLabel: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PharmacyColors.CardWhite),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Total Transactions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PharmacyColors.TextPrimary)
            Text(filterLabel, color = PharmacyColors.TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            val salesData = series?.salesData.orEmpty()
            if (salesData.isEmpty()) {
                Text(
                    "No chart data available",
                    color = PharmacyColors.TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                val sales = salesData.map { it.totalSales }
                val tx = salesData.map { it.totalTransactions.toDouble() }
                val normalizedSales = normalizeSeries(sales)
                val normalizedTx = normalizeSeries(tx)
                if (normalizedSales.isEmpty() && normalizedTx.isEmpty()) {
                    Text(
                        "No chart data available",
                        color = PharmacyColors.TextSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    val labels = salesData.map { formatSeriesLabel(it.date) }
                    TransactionChart(
                        primary = normalizedSales.ifEmpty { List(sales.size) { 0f } },
                        secondary = normalizedTx.ifEmpty { List(tx.size) { 0f } },
                        labels = labels
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionChart(primary: List<Float>, secondary: List<Float>, labels: List<String>) {
    val count = max(primary.size, secondary.size).coerceAtLeast(2)
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val width = size.width
            val height = size.height
            val spacing = width / (count - 1)

            fun List<Float>.point(i: Int): Offset {
                val value = this.getOrNull(i) ?: this.lastOrNull() ?: 0f
                return Offset(i * spacing, height - (value.coerceIn(0f, 1f) * height))
            }

            val areaPath = Path().apply {
                moveTo(0f, height)
                for (i in 0 until count) {
                    val pt = secondary.point(i)
                    lineTo(pt.x, pt.y)
                }
                lineTo(width, height)
                close()
            }
            drawPath(areaPath, PharmacyColors.ChartArea)

            for (i in 0 until count - 1) {
                val start = primary.point(i)
                val end = primary.point(i + 1)
                drawLine(
                    color = PharmacyColors.ChartLine1,
                    start = start,
                    end = end,
                    strokeWidth = 3.dp.toPx()
                )
                drawCircle(color = PharmacyColors.ChartLine1, radius = 5.dp.toPx(), center = start)
            }
            drawCircle(
                color = PharmacyColors.ChartLine1,
                radius = 5.dp.toPx(),
                center = primary.point(count - 1)
            )
            for (i in 0 until count - 1) {
                val start = secondary.point(i)
                val end = secondary.point(i + 1)
                drawLine(
                    color = PharmacyColors.ChartLine2,
                    start = start,
                    end = end,
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
        val displayedLabels = axisLabels(labels)
        if (displayedLabels.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                displayedLabels.forEach {
                    Text(it, fontSize = 10.sp, color = PharmacyColors.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun MetricsSection(summary: DashboardSummaryDto?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Patients",
                mainLabel = "Sales",
                mainValue = "${formatInt(summary?.totalTransactions ?: 0)} / ${formatMoney(summary?.totalSales ?: 0.0)}",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Cost of Sales",
                mainLabel = "Expenses",
                mainValue = "LRD ${formatMoney(summary?.totalCostOfSales ?: 0.0)}",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Value",
                mainLabel = "Customers",
                mainValue = "${formatInt(summary?.totalCustomers ?: 0)} qualified",
                additionalInfo = "Outstanding ${formatInt(summary?.qualifiedCustomers ?: 0)}",
                additionalColor = PharmacyColors.Secondary,
                modifier = Modifier.weight(1f)
            )
            InventoryGaugeCard(
                percentage = summary?.profitMargin?.roundToInt() ?: 0,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Profit Marga",
                mainLabel = "Stock",
                mainValue = "LRD ${formatMoney(summary?.currentStockValue ?: 0.0)}",
                tags = listOf(
                    "Total Stock ${formatInt(summary?.totalStock?.toInt() ?: 0)}",
                    "Profit Products"
                ),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Profit Margna",
                mainLabel = "Inventory",
                mainValue = "${formatInt(summary?.outOfStockItems ?: 0)} items",
                largeValue = "${summary?.profitMargin?.toInt() ?: 0}%",
                largeLabel = "In route downs..",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    mainLabel: String,
    mainValue: String,
    additionalInfo: String? = null,
    additionalColor: Color? = null,
    tags: List<String>? = null,
    largeValue: String? = null,
    largeLabel: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 140.dp),
        colors = CardDefaults.cardColors(containerColor = PharmacyColors.CardWhite),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PharmacyColors.TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text(mainLabel, fontSize = 12.sp, color = PharmacyColors.Secondary, fontWeight = FontWeight.Medium)
            if (mainValue.isNotEmpty()) {
                Text(mainValue, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PharmacyColors.TextPrimary)
            }
            additionalInfo?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = additionalColor ?: PharmacyColors.Secondary)
            }
            tags?.let { tagList ->
                Spacer(Modifier.height(8.dp))
                tagList.forEach { tag ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(PharmacyColors.Success, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(tag, fontSize = 10.sp, color = PharmacyColors.TextSecondary)
                    }
                }
            }
            largeValue?.let { value ->
                Spacer(Modifier.height(8.dp))
                Text(value, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = PharmacyColors.Secondary)
                largeLabel?.let { label ->
                    Text(label, fontSize = 10.sp, color = PharmacyColors.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun InventoryGaugeCard(percentage: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(containerColor = PharmacyColors.CardWhite),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Inventory", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PharmacyColors.TextPrimary)
            Spacer(Modifier.height(8.dp))
            GaugeChart(percentage = percentage.coerceIn(0, 100))
        }
    }
}

@Composable
private fun GaugeChart(percentage: Int) {
    Box(
        modifier = Modifier.size(90.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(90.dp)) {
            val strokeWidth = 12.dp.toPx()
            val radius = size.minDimension / 2 - strokeWidth / 2
            val center = Offset(size.width / 2, size.height / 2)
            drawArc(
                color = Color(0xFFE0E0E0),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
            val sweepAngle = (percentage / 100f) * 270f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(Color(0xFFFFA726), PharmacyColors.Success, PharmacyColors.Danger)
                ),
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
        }
        Text("$percentage%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PharmacyColors.TextPrimary)
    }
}

@Composable
private fun EpicSellingProductsCard(
    topProducts: List<RankedProductDto>,
    lowProducts: List<RankedProductDto>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD4EAF4)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add",
                    tint = PharmacyColors.Secondary,
                    modifier = Modifier
                        .size(24.dp)
                        .background(PharmacyColors.CardWhite, CircleShape)
                        .padding(4.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Fitty Lean Linity", fontSize = 12.sp, color = PharmacyColors.TextPrimary)
            }
            Spacer(Modifier.height(12.dp))
            Text("Epic Selling Products", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PharmacyColors.TextPrimary)
            Spacer(Modifier.height(16.dp))
            ProductItem("😊", topProducts.getOrNull(0)?.productName ?: "Stock Value", null)
            Spacer(Modifier.height(8.dp))
            ProductItem("👨‍⚕️", topProducts.getOrNull(1)?.productName ?: "Exy of Straines", null, isHighlighted = true)
            Spacer(Modifier.height(8.dp))
            ProductItem("👥", topProducts.getOrNull(2)?.productName ?: "Total Stock", "dropdown")
            Spacer(Modifier.height(8.dp))
            ProductItem("💰", lowProducts.getOrNull(0)?.productName ?: "Expenses", "8%")
        }
    }
}

@Composable
private fun ProductItem(emoji: String, title: String, action: String?, isHighlighted: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isHighlighted) Color(0xFFB3E0F2) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Text(title, fontSize = 14.sp, color = PharmacyColors.TextPrimary, modifier = Modifier.weight(1f))
        action?.let {
            when (it) {
                "dropdown" -> Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = PharmacyColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                else -> Text(it, fontSize = 12.sp, color = PharmacyColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun SummaryTableCard(summary: DashboardSummaryDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD4EAF4)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Wainnts", "Table", "Staent", "Connect").forEach { tab ->
                    Text(tab, fontSize = 12.sp, color = PharmacyColors.Secondary, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(16.dp))
            TableRow("Compact Value", null, "${formatMoney(summary.totalCostPrice)}", null)
            Divider(Modifier.padding(vertical = 8.dp))
            TableRow("Epic selling products", summary.topSellingProducts.firstOrNull()?.productName, null, PharmacyColors.Warning)
            Divider(Modifier.padding(vertical = 8.dp))
            TableRow("Profit Stock", summary.lowSellingProducts.firstOrNull()?.productName, null, PharmacyColors.Success)
            Divider(Modifier.padding(vertical = 8.dp))
            TableRow("Top MyCChart", summary.lowSellingProducts.getOrNull(1)?.productName, null, PharmacyColors.Danger)
        }
    }
}

@Composable
private fun TableRow(label1: String, label2: String?, value: String?, iconColor: Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label1, fontSize = 12.sp, color = PharmacyColors.TextPrimary, modifier = Modifier.weight(1f))
        label2?.let {
            Text(it, fontSize = 12.sp, color = PharmacyColors.TextPrimary, modifier = Modifier.weight(1f))
        }
        value?.let { Text(it, fontSize = 12.sp, color = PharmacyColors.TextSecondary) }
        iconColor?.let { color ->
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (color) {
                    PharmacyColors.Warning -> Icons.Filled.TrendingUp
                    PharmacyColors.Success -> Icons.Filled.Add
                    else -> Icons.Filled.TrendingDown
                }
                Icon(icon, contentDescription = null, tint = PharmacyColors.CardWhite, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/* ===== Share helpers and formatters ===== */

private fun shareText(ctx: android.content.Context, filename: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, filename)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(Intent.createChooser(intent, "Export dashboard"))
}

private fun normalizeSeries(values: List<Double>): List<Float> {
    if (values.isEmpty()) return emptyList()
    val maxValue = values.maxOrNull() ?: return emptyList()
    if (maxValue <= 0) return values.map { 0f }
    return values.map { (it / maxValue).toFloat().coerceIn(0f, 1f) }
}

private fun formatSeriesLabel(raw: String): String {
    return runCatching {
        LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("M/d"))
    }.getOrElse {
        raw.takeLast(5)
    }
}

private fun axisLabels(labels: List<String>, maxCount: Int = 4): List<String> {
    if (labels.isEmpty()) return emptyList()
    if (labels.size <= maxCount) return labels
    val step = (labels.size - 1).toFloat() / (maxCount - 1)
    return (0 until maxCount).map { idx ->
        val index = (idx * step).roundToInt().coerceIn(0, labels.lastIndex)
        labels[index]
    }
}

private fun quickFilterLabel(value: String): String = when (value) {
    "last7days" -> "Last 7 days"
    "last30days" -> "Last 30 days"
    "thisMonth" -> "This month"
    else -> value.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun formatMoney(v: Double): String = "%,.2f".format(v)

private fun formatInt(v: Int): String = "%,d".format(v)
