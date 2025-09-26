package com.sitsofe.scanner.core.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE ASC")
    fun pagingAll(): PagingSource<Int, ProductEntity>

    @Query(
        """
        SELECT * FROM products
        WHERE name LIKE '%' || :q || '%'
           OR barcode LIKE '%' || :q || '%'
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun pagingSearch(q: String): PagingSource<Int, ProductEntity>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clear()

    // NEW: used by initialSyncIfNeeded to decide if a network pull is needed
    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    // NEW: resolve cart lines by product IDs
    @Query("SELECT * FROM products WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<ProductEntity>
}
