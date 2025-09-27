package com.sitsofe.scanner.feature.products

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sitsofe.scanner.core.data.ProductRepository

@Suppress("UNCHECKED_CAST")
class ProductsVMFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = ProductRepository.from(context)
        return ProductsViewModel(repo) as T
    }
}
