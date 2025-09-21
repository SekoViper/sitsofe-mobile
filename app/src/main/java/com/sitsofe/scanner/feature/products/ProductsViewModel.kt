package com.sitsofe.scanner.feature.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.sitsofe.scanner.core.data.ProductRepository
import com.sitsofe.scanner.core.db.ProductEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val repo: ProductRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    val items: Flow<PagingData<ProductEntity>> =
        query.debounce(300).flatMapLatest { repo.searchPaged(it) }

    fun onQueryChange(q: String) { query.value = q }

    fun initialSyncOnce() {
        viewModelScope.launch { runCatching { repo.initialSync() } }
    }
}
