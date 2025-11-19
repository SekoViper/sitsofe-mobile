package com.sitsofe.scanner.core.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dashboard_summary_cache")
data class DashboardSummaryCacheEntity(
    @PrimaryKey val cacheKey: String = "default",
    val payload: String,
    val updatedAt: Long
)

@Entity(tableName = "dashboard_series_cache")
data class DashboardSeriesCacheEntity(
    @PrimaryKey val filterType: String,
    val payload: String,
    val updatedAt: Long
)
