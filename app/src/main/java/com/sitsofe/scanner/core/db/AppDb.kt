package com.sitsofe.scanner.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        DashboardSummaryCacheEntity::class,
        DashboardSeriesCacheEntity::class
    ],
    version = 2
)
abstract class AppDb : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun dashboardDao(): DashboardCacheDao

    companion object {
        @Volatile private var INSTANCE: AppDb? = null
        fun get(ctx: Context): AppDb =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(ctx, AppDb::class.java, "sitsofe.db")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
