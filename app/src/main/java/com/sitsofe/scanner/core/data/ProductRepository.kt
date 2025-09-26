package com.sitsofe.scanner.core.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sitsofe.scanner.core.db.AppDb
import com.sitsofe.scanner.core.db.ProductEntity
import com.sitsofe.scanner.core.network.Api
import com.sitsofe.scanner.core.network.toEntity
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class ProductRepository(
    private val db: AppDb,
    private val api: Api? = null
) {
    fun pager(query: String): Flow<PagingData<ProductEntity>> {
        val dao = db.productDao()
        val source = if (query.isBlank()) dao.pagingAll() else dao.pagingSearch(query)
        return Pager(PagingConfig(pageSize = 40, prefetchDistance = 20)) { source }.flow
    }

    suspend fun findByBarcode(code: String): ProductEntity? =
        db.productDao().getByBarcode(code.trim())

    suspend fun initialSyncIfNeeded() {
        val dao = db.productDao()
        val cur = try { dao.count() } catch (e: Exception) {
            Timber.tag("SYNC").e(e, "Failed to count products; will try syncing anyway")
            0
        }
        if (cur > 0) {
            Timber.tag("SYNC").i("Products already cached: %d. Skipping initial sync.", cur)
            return
        }

        if (api == null) {
            Timber.tag("SYNC").w("No Api provided to ProductRepository; cannot sync.")
            return
        }

        try {
            Timber.tag("SYNC").i("Fetching products from backend…")
            // Prefer full details if your list isn’t massive; otherwise switch to the minimal endpoint.
            val remote = api.productsFull()
            val entities = remote.map { it.toEntity() }

            // Simple strategy: reset and insert
            dao.clear()
            dao.upsertAll(entities)

            Timber.tag("SYNC").i("Synced %d products into Room.", entities.size)
        } catch (t: Throwable) {
            Timber.tag("SYNC").e(t, "Initial products sync failed")
        }
    }

    companion object {
        fun from(context: Context): ProductRepository =
            ProductRepository(AppDb.get(context))

        fun from(context: Context, api: Api): ProductRepository =
            ProductRepository(AppDb.get(context), api)
    }
}
