package com.sitsofe.scanner.feature.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sitsofe.scanner.core.data.ProductRepository
import com.sitsofe.scanner.core.db.ProductEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repo: ProductRepository
) : ViewModel() {

    // ───── search ──────────────────────────────────────────────────────────────
    private val queryFlow = MutableStateFlow("")
    var query: String = ""
        private set
    fun onQueryChange(q: String) {
        query = q
        queryFlow.value = q
    }

    // ───── paging ──────────────────────────────────────────────────────────────
    val items: Flow<PagingData<ProductEntity>> =
        queryFlow.debounce(200)
            .flatMapLatest { repo.pager(it) }
            .cachedIn(viewModelScope)

    // ───── cart (id -> qty) ────────────────────────────────────────────────────
    private val _cart = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cart: StateFlow<Map<String, Int>> = _cart

    // badge count
    val cartCount: StateFlow<Int> =
        _cart.map { it.values.sum() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private fun setQty(id: String, qty: Int) {
        _cart.update { cur -> if (qty <= 0) cur - id else cur + (id to qty) }
    }

    fun add(p: ProductEntity, qty: Int = 1) {
        val cur = _cart.value[p.id] ?: 0
        setQty(p.id, cur + qty)
    }
    fun remove(p: ProductEntity) = add(p, -1)
    fun clearCart() { _cart.value = emptyMap() }

    // ───── events ──────────────────────────────────────────────────────────────
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events

    fun initialSyncOnce() {
        viewModelScope.launch { repo.initialSyncIfNeeded() }
    }

    // used by scanner and manual inputs
    fun addByBarcode(raw: String) {
        val code = raw.trim().removeSuffix("\n").removeSuffix("\r")
        viewModelScope.launch {
            val p = repo.findByBarcode(code)
            if (p != null) {
                add(p)
                _events.emit("${p.name} (+1)")
            } else {
                _events.emit("No product for barcode: $code")
            }
        }
    }

    // Helpers for CartScreen
    suspend fun productsByIds(ids: List<String>): List<ProductEntity> = repo.getByIds(ids)
}
