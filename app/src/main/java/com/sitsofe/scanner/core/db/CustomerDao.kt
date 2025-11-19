package com.sitsofe.scanner.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAll(): List<CustomerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CustomerEntity>)

    @Query("DELETE FROM customers")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(items: List<CustomerEntity>) {
        clear()
        if (items.isNotEmpty()) {
            upsertAll(items)
        }
    }
}
