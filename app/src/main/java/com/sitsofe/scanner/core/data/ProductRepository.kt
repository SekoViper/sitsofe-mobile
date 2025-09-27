package com.sitsofe.scanner.core.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sitsofe.scanner.core.db.AppDb
import com.sitsofe.scanner.core.db.ProductEntity
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class ProductRepository(
    private val db: AppDb
) {
    fun pager(query: String): Flow<PagingData<ProductEntity>> {
        val dao = db.productDao()
        val source = if (query.isBlank()) dao.pagingAll() else dao.pagingSearch(query)
        return Pager(PagingConfig(pageSize = 40, prefetchDistance = 20)) { source }.flow
    }

    suspend fun findByBarcode(code: String): ProductEntity? =
        db.productDao().getByBarcode(code.trim())

    suspend fun getByIds(ids: List<String>): List<ProductEntity> =
        if (ids.isEmpty()) emptyList() else db.productDao().getByIds(ids)

    /** Safe no-op if your DB is already seeded. */
    suspend fun initialSyncIfNeeded() {
        val count = runCatching { db.productDao().count() }.getOrElse { 0 }
        Timber.tag("SYNC").i("initialSyncIfNeeded(): products in DB = %d", count)
        // If you later add a products endpoint, pull & upsert here.
    }

    companion object {
        fun from(context: Context): ProductRepository = ProductRepository(AppDb.get(context))
    }
}
