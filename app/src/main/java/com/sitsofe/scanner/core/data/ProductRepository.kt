package com.sitsofe.scanner.core.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sitsofe.scanner.core.db.AppDb
import com.sitsofe.scanner.core.db.ProductEntity
import com.sitsofe.scanner.core.network.Api
import com.sitsofe.scanner.core.network.toEntity
import com.sitsofe.scanner.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber

class ProductRepository(
    private val db: AppDb,
    private val api: Api = ServiceLocator.api()  // interceptor adds Authorization + X-* headers
) {
    /** Paging source for products (Room-backed). */
    fun pager(query: String): Flow<PagingData<ProductEntity>> {
        val dao = db.productDao()
        val source = if (query.isBlank()) dao.pagingAll() else dao.pagingSearch(query)
        return Pager(PagingConfig(pageSize = 40, prefetchDistance = 20)) { source }.flow
    }

    /** Local lookup by barcode (fast; used by scanner and manual entry). */
    suspend fun findByBarcode(code: String): ProductEntity? =
        db.productDao().getByBarcode(code.trim())

    /** Resolve a set of product IDs for cart/checkout rendering. */
    suspend fun getByIds(ids: List<String>): List<ProductEntity> =
        if (ids.isEmpty()) emptyList() else db.productDao().getByIds(ids)

    /** Seed from backend if DB is empty (first launch on this tenant/subsidiary). */
    suspend fun initialSyncIfNeeded() {
        val dao = db.productDao()
        val count = runCatching { dao.count() }.getOrElse { 0 }
        if (count > 0) {
            Timber.tag("SYNC").i("Products already cached: %d → skip initial sync", count)
            return
        }
        seedFromApi(dao)
    }

    /** Optional manual refresh (pull-to-refresh etc.). */
    suspend fun refreshProducts() {
        seedFromApi(db.productDao())
    }

    private suspend fun seedFromApi(dao: com.sitsofe.scanner.core.db.ProductDao) = withContext(Dispatchers.IO) {
        runCatching {
            Timber.tag("SYNC").i("Fetching subsidiary products from backend…")
            val remote = api.subsidiaryProducts()
            val entities = remote.map { it.toEntity() }
            dao.clear()
            dao.upsertAll(entities)
            Timber.tag("SYNC").i("Seeded %d products", entities.size)
        }.onFailure { t ->
            Timber.tag("SYNC").e(t, "Failed to sync products")
            // If you see 403/401 here, verify headers in ServiceLocator/Network are complete.
        }
    }

    companion object {
        fun from(context: Context): ProductRepository =
            ProductRepository(AppDb.get(context))
    }
}
