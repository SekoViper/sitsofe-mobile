package com.sitsofe.scanner.di

import com.sitsofe.scanner.core.data.ProductRepository
import com.sitsofe.scanner.core.data.ProductRepositoryImpl
import com.sitsofe.scanner.core.db.ProductDao
import com.sitsofe.scanner.core.network.Api
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductRepository(api: Api, productDao: ProductDao): ProductRepository {
        return ProductRepositoryImpl(api, productDao)
    }
}
