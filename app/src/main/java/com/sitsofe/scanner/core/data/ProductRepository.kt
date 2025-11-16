package com.sitsofe.scanner.core.data

import androidx.paging.PagingData
import com.sitsofe.scanner.core.db.ProductEntity
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun pager(query: String): Flow<PagingData<ProductEntity>>
    suspend fun findByBarcode(code: String): ProductEntity?
    suspend fun getByIds(ids: List<String>): List<ProductEntity>
    suspend fun initialSyncIfNeeded()
    suspend fun refreshProducts()
}
