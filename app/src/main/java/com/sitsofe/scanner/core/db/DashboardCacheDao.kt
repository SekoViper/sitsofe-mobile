package com.sitsofe.scanner.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DashboardCacheDao {

    @Query("SELECT * FROM dashboard_summary_cache LIMIT 1")
    suspend fun getSummary(): DashboardSummaryCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSummary(entity: DashboardSummaryCacheEntity)

    @Query("SELECT * FROM dashboard_series_cache WHERE filterType = :filter LIMIT 1")
    suspend fun getSeries(filter: String): DashboardSeriesCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSeries(entity: DashboardSeriesCacheEntity)
}
