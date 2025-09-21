package com.sitsofe.scanner.feature.products

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sitsofe.scanner.di.ServiceLocator

class ProductsVMFactory(private val ctx: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProductsViewModel(ServiceLocator.productRepository(ctx)) as T
    }
}
