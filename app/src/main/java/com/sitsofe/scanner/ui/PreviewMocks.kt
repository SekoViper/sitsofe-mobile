package com.sitsofe.scanner.ui

import android.content.Context
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.sitsofe.scanner.core.auth.Session
import com.sitsofe.scanner.core.auth.SessionPrefs
import com.sitsofe.scanner.core.data.ProductRepository
import com.sitsofe.scanner.core.db.AppDb
import com.sitsofe.scanner.core.db.ProductDao
import com.sitsofe.scanner.core.db.ProductEntity
import com.sitsofe.scanner.core.network.*
import com.sitsofe.scanner.feature.checkout.CheckoutViewModel
import com.sitsofe.scanner.feature.dashboard.DashboardViewModel
import com.sitsofe.scanner.feature.products.ProductsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.Response

object PreviewMocks {

    class MockApi : Api {
        override suspend fun login(req: LoginRequest): LoginResponse {
            TODO("Not yet implemented")
        }

        override suspend fun subsidiaryProducts(): List<ProductDto> {
            TODO("Not yet implemented")
        }

        override suspend fun customers(): List<CustomerDto> {
            return (1..5).map {
                CustomerDto(
                    _id = "id$it",
                    name = "Customer $it",
                    phone = "123456789$it"
                )
            }
        }

        override suspend fun createSale(req: SalesRequest): Response<SalesResponse> {
            TODO("Not yet implemented")
        }

        override suspend fun dashboardSummary(): DashboardSummaryDto {
            return DashboardSummaryDto(
                totalSales = 12345.67,
                totalCashSales = 12000.0,
                internalSalesCost = 345.67,
                totalTransactions = 123,
                totalCostOfSales = 8000.0,
                grossProfit = 4345.67,
                netProfit = 3000.0,
                profitMargin = 24.3,
                totalPurchaseAmount = 5000.0,
                currentStockValue = 10000.0,
                totalCostPrice = 7000.0,
                totalStock = 500,
                outOfStockItems = 10,
                expiringProducts = 5,
                expiredProducts = 2,
                salesByCategory = listOf(
                    CategorySalesDto("Category 1", 5000.0),
                    CategorySalesDto("Category 2", 7345.67)
                ),
                topSellingProducts = listOf(
                    ProductSalesDto("Product 1", 100),
                    ProductSalesDto("Product 2", 50)
                ),
                lowSellingProducts = listOf(
                    ProductSalesDto("Product 3", 5),
                    ProductSalesDto("Product 4", 2)
                ),
                totalCustomers = 50,
                qualifiedCustomers = 20,
                expensesTotal = 1345.67
            )
        }

        override suspend fun dashboardSummaryRange(start: String, end: String): DashboardSummaryDto {
            TODO("Not yet implemented")
        }

        override suspend fun dashboardSeries(filter: String): DashboardSeriesDto {
            return DashboardSeriesDto(
                salesData = listOf(
                    SeriesDataDto("2023-01-01", 1000.0, 10),
                    SeriesDataDto("2023-01-02", 1500.0, 15),
                    SeriesDataDto("2023-01-03", 1200.0, 12),
                    SeriesDataDto("2023-01-04", 1800.0, 18),
                    SeriesDataDto("2023-01-05", 2000.0, 20),
                    SeriesDataDto("2023-01-06", 2500.0, 25),
                    SeriesDataDto("2023-01-07", 2200.0, 22)
                )
            )
        }
    }

    class MockProductDao : ProductDao {
        override fun pagingAll(): PagingSource<Int, ProductEntity> {
            TODO("Not yet implemented")
        }

        override fun pagingSearch(query: String): PagingSource<Int, ProductEntity> {
            TODO("Not yet implemented")
        }

        override suspend fun getByBarcode(barcode: String): ProductEntity? = null

        override suspend fun getByIds(ids: List<String>): List<ProductEntity> = emptyList()

        override suspend fun count(): Int = 0

        override suspend fun upsertAll(products: List<ProductEntity>) {}

        override suspend fun clear() {}
    }

    class MockAppDb(context: Context) : AppDb() {
        override fun productDao(): ProductDao = MockProductDao()
        override fun clearAllTables() {}
        override fun close() {}
    }

    class MockSessionPrefs(context: Context) : SessionPrefs(context) {

        override fun save(s: Session) {}

        override fun load(): Session? = null

        override fun clear() {}

        override fun setLastEmail(email: String) {}

        override fun getLastEmail(): String? = null
    }

    class MockProductRepository : ProductRepository {
        val products = (1..10).map {
            ProductEntity(
                id = "id$it",
                name = "Product $it",
                price = it * 10.0,
                stock = it * 2,
                barcode = "barcode$it",
                categoryName = "Category ${it % 3}"
            )
        }

        override fun pager(query: String): Flow<PagingData<ProductEntity>> {
            return flowOf(PagingData.from(products))
        }

        override suspend fun findByBarcode(code: String): ProductEntity? = null
        override suspend fun getByIds(ids: List<String>): List<ProductEntity> {
            return products.filter { it.id in ids }
        }

        override suspend fun initialSyncIfNeeded() {}
        override suspend fun refreshProducts() {}
    }

    val productsViewModel = ProductsViewModel(MockProductRepository()).apply {
        add(ProductEntity("id1", "Product 1", 10.0, 2, "barcode1", "Category 0"), 2)
        add(ProductEntity("id2", "Product 2", 20.0, 4, "barcode2", "Category 1"), 1)
    }
    val dashboardViewModel = DashboardViewModel(MockApi())
    val checkoutViewModel = CheckoutViewModel(MockApi(), productsViewModel)
}
