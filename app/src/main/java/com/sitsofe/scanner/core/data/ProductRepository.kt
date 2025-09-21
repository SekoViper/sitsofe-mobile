package com.sitsofe.scanner.core.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sitsofe.scanner.core.db.ProductDao
import com.sitsofe.scanner.core.db.ProductEntity
import com.sitsofe.scanner.core.network.Api
import com.sitsofe.scanner.core.network.ProductDto
import com.sitsofe.scanner.core.network.ProductNameIdDto
import com.sitsofe.scanner.core.network.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber

class ProductRepository(
    private val api: Api,
    private val dao: ProductDao
) {
    suspend fun initialSync(): Pair<Int, Int> = withContext(Dispatchers.IO) {
        Timber.tag("SYNC").i("Starting product sync…")

        val entities: List<ProductEntity> = runCatching {
            Timber.tag("SYNC").d("Fetching FULL products…")
            val list = api.productsFull()
            Timber.tag("SYNC").i("Full fetch OK: %d items", list.size)
            list.map { it.toEntity() }
        }.getOrElse { fullErr ->
            Timber.tag("SYNC").w(fullErr, "Full fetch FAILED. Trying MIN list…")
            val min: List<ProductNameIdDto> = api.productsMin()
            Timber.tag("SYNC").i("Min fetch OK: %d items", min.size)
            min.map { it.minToEntity() }
        }

        Timber.tag("SYNC").d("Replacing local cache…")
        dao.clear()
        var inserted = 0
        entities.chunked(500).forEach { batch ->
            dao.upsertAll(batch)
            inserted += batch.size
        }
        Timber.tag("SYNC").i("Product sync COMPLETE. Inserted=%d", inserted)
        inserted to entities.size
    }

    fun searchPaged(query: String): Flow<PagingData<ProductEntity>> =
        Pager(PagingConfig(pageSize = 30, enablePlaceholders = false)) {
            dao.searchPaging(query.trim())
        }.flow

    suspend fun byBarcode(barcode: String): ProductEntity? = withContext(Dispatchers.IO) {
        dao.idByBarcode(barcode)?.let { dao.byId(it) }
    }

    private fun ProductNameIdDto.minToEntity(): ProductEntity =
        ProductDto(
            _id = _id,
            name = name,
            price = 0.0,
            discount_price = 0.0,
            cost_price = 0.0,
            stock = 0.0,
            barcode = barcode,
            category_name = null
        ).toEntity()
}
