package com.sitsofe.scanner.core.db

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface ProductDao {
    @Query("""
       SELECT * FROM products
       WHERE (:q == '' OR name LIKE :q || '%' OR barcode LIKE :q || '%')
       ORDER BY name
    """)
    fun searchPaging(q: String): PagingSource<Int, ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clear()

    @Query("SELECT * FROM products WHERE id=:id LIMIT 1")
    suspend fun byId(id: String): ProductEntity?

    @Query("SELECT id FROM products WHERE barcode=:code LIMIT 1")
    suspend fun idByBarcode(code: String): String?
}
