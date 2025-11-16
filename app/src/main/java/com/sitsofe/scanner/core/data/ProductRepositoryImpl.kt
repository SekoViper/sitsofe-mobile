package com.sitsofe.scanner.core.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sitsofe.scanner.core.db.ProductDao
import com.sitsofe.scanner.core.db.ProductEntity
import com.sitsofe.scanner.core.network.Api
import com.sitsofe.scanner.core.network.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: Api, 
    private val productDao: ProductDao
) : ProductRepository {
    override fun pager(query: String): Flow<PagingData<ProductEntity>> {
        val source = if (query.isBlank()) productDao.pagingAll() else productDao.pagingSearch(query)
        return Pager(PagingConfig(pageSize = 40, prefetchDistance = 20)) { source }.flow
    }

    override suspend fun findByBarcode(code: String): ProductEntity? =
        productDao.getByBarcode(code.trim())

    override suspend fun getByIds(ids: List<String>): List<ProductEntity> =
        if (ids.isEmpty()) emptyList() else productDao.getByIds(ids)

    override suspend fun initialSyncIfNeeded() {
        val count = runCatching { productDao.count() }.getOrElse { 0 }
        if (count > 0) {
            Timber.tag("SYNC").i("Products already cached: %d → skip initial sync", count)
            return
        }
        seedFromApi(productDao)
    }

    override suspend fun refreshProducts() {
        seedFromApi(productDao)
    }

    private suspend fun seedFromApi(dao: ProductDao) = withContext(Dispatchers.IO) {
        runCatching {
            Timber.tag("SYNC").i("Fetching subsidiary products from backend…")
            val remote = api.subsidiaryProducts()
            val entities = remote.map { it.toEntity() }
            dao.clear()
            dao.upsertAll(entities)
            Timber.tag("SYNC").i("Seeded %d products", entities.size)
        }.onFailure { t ->
            Timber.tag("SYNC").e(t, "Failed to sync products")
        }
    }
}
