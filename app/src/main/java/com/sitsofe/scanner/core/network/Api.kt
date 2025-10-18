package com.sitsofe.scanner.core.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

interface Api {

    // ── AUTH ───────────────────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    // ── PRODUCTS (Subsidiary-scoped) ───────────────────────────────────────────
    @GET("products/subsidiary_products")
    suspend fun subsidiaryProducts(): List<SubsidiaryProductDto>

    // ── CUSTOMERS / SALES ─────────────────────────────────────────────────────
    @GET("customers")
    suspend fun customers(): List<CustomerDto>

    @POST("sales")
    suspend fun createSale(@Body body: SalesRequest): SalesResponse

    // ── DASHBOARD ─────────────────────────────────────────────────────────────
    // Summary – no date filter
    @GET("dashboard/subsidiary")
    suspend fun dashboardSummary(): DashboardSummaryDto

    // Summary – with explicit date range
    @GET("dashboard/subsidiary")
    suspend fun dashboardSummaryRange(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): DashboardSummaryDto

    // Time-series (e.g., last7days)
    @GET("dashboard/filter-sales")
    suspend fun dashboardSeries(@Query("filterType") filterType: String): DashboardSeriesDto
}

/* ===================== AUTH ===================== */
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val user: LoginUser, val tenant: LoginTenant)
data class LoginUser(
    @SerializedName("_id") val id: String,
    @SerializedName("tenant_id") val tenantId: String,
    val name: String, val email: String, val phone: String?,
    val role: String,
    @SerializedName("subsidiary_id") val subsidiaryId: String
)
data class LoginTenant(
    @SerializedName("tenant_id") val tenantId: String,
    val name: String, val currency: String
)

/* ===================== PRODUCTS ===================== */
data class SubsidiaryProductDto(
    @SerializedName("_id") val id: String,
    @SerializedName("tenant_id") val tenantId: String?,
    @SerializedName("subsidiary_id") val subsidiaryId: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("discount_price") val discountPrice: Double?,
    @SerializedName("cost_price") val costPrice: Double?,
    @SerializedName("stock") val stock: Double,
    @SerializedName("barcode") val barcode: String?,
    @SerializedName("expiry_date") val expiryDate: String?,
    @SerializedName("supplier_name") val supplierName: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("point_allocation_method") val pointAllocationMethod: String?,
    @SerializedName("percentage_rate") val percentageRate: Double?,
    @SerializedName("fixed_points") val fixedPoints: Double?,
    @SerializedName("bonus_points") val bonusPoints: Double?
)

private fun String?.nullIfLiteral(): String? =
    this?.takeIf { it.isNotBlank() && it.lowercase() != "null" }

fun SubsidiaryProductDto.toEntity(): com.sitsofe.scanner.core.db.ProductEntity =
    com.sitsofe.scanner.core.db.ProductEntity(
        id = id,
        name = name,
        barcode = barcode.nullIfLiteral() ?: "",
        price = price,
        stock = stock,
        costPrice = costPrice ?: 0.0,
        categoryName = categoryName.nullIfLiteral()
    )

/* ===================== CUSTOMERS/SALES ===================== */
data class CustomerDto(
    @SerializedName("_id") val _id: String,
    val name: String,
    val phone: String
)
data class SalesItem(val product_id: String, val quantity: Int, val price: Double, val name: String)
data class SalesRequest(
    val customer_phone: String?,
    val customer_type: String,
    val payment_method: String,
    val items: List<SalesItem>,
    val condition: String? = null
)
data class SalesResponse(val id: String? = null, val message: String? = null)

/* ===================== DASHBOARD ===================== */
// Summary
data class CategorySalesDto(
    val totalSales: Double,
    val categoryId: String,
    val categoryName: String
)
data class RankedProductDto(
    @SerializedName("_id") val id: String,
    val totalSold: Int,
    val productId: String,
    val productName: String
)
data class DashboardSummaryDto(
    val totalCashSales: Double,
    val totalInsuranceSales: Double,
    val totalInsurancePaid: Double,
    val totalInsurancePending: Double,
    val totalSales: Double,
    val totalTransactions: Int,
    val totalCostOfSales: Double,
    val internalSalesCost: Double,
    val grossProfit: Double,
    val expensesTotal: Double,
    val totalPurchaseAmount: Double,
    val netProfit: Double,
    val currentStockValue: Double,
    val totalStock: Double,
    val totalCostPrice: Double,
    val profitMargin: Double,
    val totalCustomers: Int,
    val qualifiedCustomers: Int,
    val outOfStockItems: Int,
    val expiringProducts: Int,
    val expiredProducts: Int,
    val topSellingProducts: List<RankedProductDto>,
    val lowSellingProducts: List<RankedProductDto>,
    val salesByCategory: List<CategorySalesDto>
)

// Series (chart)
data class DaySeriesDto(
    val date: String,
    val totalSales: Double,
    val totalTransactions: Int,
    val internalSalesCost: Double
)
data class DashboardSeriesDto(
    val filterType: String,
    val salesData: List<DaySeriesDto>,
    val startDate: String,
    val endDate: String
)
