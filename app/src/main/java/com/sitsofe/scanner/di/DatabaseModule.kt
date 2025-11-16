package com.sitsofe.scanner.di

import android.content.Context
import com.sitsofe.scanner.core.db.AppDb
import com.sitsofe.scanner.core.db.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDb(@ApplicationContext context: Context): AppDb {
        return AppDb.get(context)
    }
    @Provides
    @Singleton
    fun provideProductDao(appDb: AppDb): ProductDao {
        return appDb.productDao()
    }
}
