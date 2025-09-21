package com.sitsofe.scanner.core.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sitsofe.scanner.core.db.AppDb
import com.sitsofe.scanner.core.db.ProductEntity
import com.sitsofe.scanner.core.network.Api
import kotlinx.coroutines.flow.Flow


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
        // later: pull from api and upsert into db
    }

    companion object {
        fun from(context: Context): ProductRepository =
            ProductRepository(AppDb.get(context))

        fun from(context: Context, api: Api): ProductRepository =
            ProductRepository(AppDb.get(context), api)
    }
}
