package com.sitsofe.scanner.feature.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.sitsofe.scanner.core.db.AppDb
import com.sitsofe.scanner.core.db.DashboardSeriesCacheEntity
import com.sitsofe.scanner.core.db.DashboardSummaryCacheEntity
import com.sitsofe.scanner.core.network.DashboardSeriesDto
import com.sitsofe.scanner.core.network.DashboardSummaryDto
import com.sitsofe.scanner.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardViewModel(
    private val appContext: Context,
    private val api: com.sitsofe.scanner.core.network.Api = ServiceLocator.api()
) : ViewModel() {

    private val dashboardDao = AppDb.get(appContext).dashboardDao()
    private val gson = Gson()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _summary = MutableStateFlow<DashboardSummaryDto?>(null)
    val summary: StateFlow<DashboardSummaryDto?> = _summary

    private val _series = MutableStateFlow<DashboardSeriesDto?>(null)
    val series: StateFlow<DashboardSeriesDto?> = _series

    private val _filter = MutableStateFlow("last7days")
    val filter: StateFlow<String> = _filter

    private val dfApi: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun initLoad() {
        if (_loading.value) return
        viewModelScope.launch {
            if (_summary.value == null) {
                dashboardDao.getSummary()?.let { entity ->
                    runCatching { gson.fromJson(entity.payload, DashboardSummaryDto::class.java) }
                        .onSuccess { _summary.value = it }
                }
            }
            if (_series.value == null) {
                dashboardDao.getSeries(_filter.value)?.let { entity ->
                    runCatching { gson.fromJson(entity.payload, DashboardSeriesDto::class.java) }
                        .onSuccess { _series.value = it }
                }
            }
            refreshSummaryAndSeries(_filter.value)
        }
    }

    fun setQuickFilter(filterType: String) {
        if (_filter.value == filterType) return
        _filter.value = filterType
        viewModelScope.launch {
            dashboardDao.getSeries(filterType)?.let { entity ->
                runCatching { gson.fromJson(entity.payload, DashboardSeriesDto::class.java) }
                    .onSuccess { _series.value = it }
            }
            runCatching { api.dashboardSeries(filterType) }
                .onSuccess {
                    _series.value = it
                    dashboardDao.saveSeries(
                        DashboardSeriesCacheEntity(
                            filterType = filterType,
                            payload = gson.toJson(it),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
                .onFailure { Timber.e(it, "Series load failed") }
        }
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            _loading.value = true
            val s = start.format(dfApi)
            val e = end.format(dfApi)
            runCatching { api.dashboardSummaryRange(s, e) }
                .onSuccess {
                    _summary.value = it
                    dashboardDao.saveSummary(
                        DashboardSummaryCacheEntity(
                            payload = gson.toJson(it),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
                .onFailure { Timber.e(it, "Summary range failed") }
            _loading.value = false
        }
    }

    private suspend fun refreshSummaryAndSeries(filterType: String) {
        _loading.value = true
        runCatching { api.dashboardSummary() }
            .onSuccess {
                _summary.value = it
                dashboardDao.saveSummary(
                    DashboardSummaryCacheEntity(
                        payload = gson.toJson(it),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            .onFailure { Timber.e(it, "Dashboard summary failed") }
        runCatching { api.dashboardSeries(filterType) }
            .onSuccess {
                _series.value = it
                dashboardDao.saveSeries(
                    DashboardSeriesCacheEntity(
                        filterType = filterType,
                        payload = gson.toJson(it),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            .onFailure { Timber.e(it, "Dashboard series failed") }
        _loading.value = false
    }

    /** Build an export text (CSV-ish) to share. */
    fun exportText(): String {
        val sum = _summary.value ?: return "No data"
        val sb = StringBuilder()
        sb.appendLine("Section,Metric,Value")
        sb.appendLine("Sales,Total Sales,${sum.totalSales}")
        sb.appendLine("Sales,Cash Sales,${sum.totalCashSales}")
        sb.appendLine("Sales,Internal Sales Cost,${sum.internalSalesCost}")
        sb.appendLine("Sales,Transactions,${sum.totalTransactions}")
        sb.appendLine("Profitability,Cost Of Sales,${sum.totalCostOfSales}")
        sb.appendLine("Profitability,Gross Profit,${sum.grossProfit}")
        sb.appendLine("Profitability,Net Profit,${sum.netProfit}")
        sb.appendLine("Profitability,Profit Margin %,${sum.profitMargin}")
        sb.appendLine("Inventory,Purchase Amount,${sum.totalPurchaseAmount}")
        sb.appendLine("Inventory,Current Stock Value,${sum.currentStockValue}")
        sb.appendLine("Inventory,Cost Price,${sum.totalCostPrice}")
        sb.appendLine("Inventory,Total Stock,${sum.totalStock}")
        sb.appendLine("Inventory,Out of Stock,${sum.outOfStockItems}")
        sb.appendLine("Inventory,Expiring,${sum.expiringProducts}")
        sb.appendLine("Inventory,Expired,${sum.expiredProducts}")
        sum.salesByCategory.forEach {
            sb.appendLine("Category,${it.categoryName},${it.totalSales}")
        }
        sb.appendLine("Top Selling Products,Name,Total Sold")
        sum.topSellingProducts.forEach { sb.appendLine(",${it.productName},${it.totalSold}") }
        sb.appendLine("Low Selling Products,Name,Total Sold")
        sum.lowSellingProducts.forEach { sb.appendLine(",${it.productName},${it.totalSold}") }
        val ser = _series.value
        ser?.let {
            sb.appendLine("Series,Date,Sales,Transactions")
            it.salesData.forEach { d -> sb.appendLine(",${d.date},${d.totalSales},${d.totalTransactions}") }
        }
        return sb.toString()
    }
}
