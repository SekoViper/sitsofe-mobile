package com.sitsofe.scanner.feature.checkout

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sitsofe.scanner.core.data.toDto
import com.sitsofe.scanner.core.data.toEntity
import com.sitsofe.scanner.core.db.AppDb
import com.sitsofe.scanner.core.network.CustomerDto
import com.sitsofe.scanner.core.network.SalesItem
import com.sitsofe.scanner.core.network.SalesRequest
import com.sitsofe.scanner.di.ServiceLocator
import com.sitsofe.scanner.feature.products.ProductsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CheckoutViewModel(
    private val appContext: Context,
    private val productsVM: com.sitsofe.scanner.feature.products.ProductsViewModel
) : ViewModel() {

    private val api = ServiceLocator.api()
    private val customerDao = AppDb.get(appContext).customerDao()

    // UI state
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _customers = MutableStateFlow<List<CustomerDto>>(emptyList())
    val customers: StateFlow<List<CustomerDto>> = _customers.asStateFlow()

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search.asStateFlow()
    fun onSearchChange(v: String) { _search.value = v.take(4) }  // last 4 digits only

    private val _selected = MutableStateFlow<CustomerDto?>(null)
    val selected: StateFlow<CustomerDto?> = _selected.asStateFlow()
    fun clearSelection() { _selected.value = null }

    private val _internal = MutableStateFlow(false)
    val internal: StateFlow<Boolean> = _internal.asStateFlow()
    fun toggleInternal() {
        _internal.value = !_internal.value
        if (_internal.value) _selected.value = null
    }

    private val _condition = MutableStateFlow("")
    val condition: StateFlow<String> = _condition.asStateFlow()
    fun onConditionChange(v: String) { _condition.value = v }

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()
    fun clearToast() { _toast.value = null }

    fun loadCustomers(force: Boolean = false) {
        if (_loading.value) return
        viewModelScope.launch {
            _loading.value = true

            // Serve cached contacts first for instant offline UX.
            val cached = runCatching { customerDao.getAll() }.getOrDefault(emptyList())
            if (cached.isNotEmpty() && !force) {
                _customers.value = cached.map { it.toDto() }
            }

            runCatching { api.customers() }
                .onSuccess { fresh ->
                    _customers.value = fresh
                    customerDao.replaceAll(fresh.map { it.toEntity() })
                }
                .onFailure { t ->
                    Timber.e(t, "Failed to load customers")
                    if (cached.isEmpty()) {
                        _toast.value = "Failed to load customers"
                    } else {
                        _toast.value = "Showing cached customers"
                    }
                }
            _loading.value = false
        }
    }

    fun selectCustomer(c: CustomerDto) {
        _selected.value = c
        _internal.value = false
    }

    /**
     * Build the payload and post to /api/sales.
     */
    fun completeSale(
        lines: List<Pair<com.sitsofe.scanner.core.db.ProductEntity, Int>>,
        onSuccess: () -> Unit
    ) {
        if (lines.isEmpty()) {
            _toast.value = "Cart is empty"
            return
        }

        val items = lines.map { (p, q) ->
            SalesItem(
                product_id = p.id,
                quantity = q,
                price = p.price,
                name = p.name
            )
        }

        val isInternal = _internal.value
        val sel = _selected.value

        // Validate selection (either internal OR a customer)
        if (!isInternal && sel == null) {
            _toast.value = "Please select a customer or choose Internal Sales"
            return
        }

        val body = SalesRequest(
            customer_phone = if (isInternal) null else sel!!.phone,
            customer_type = if (isInternal) "owner" else "regular",
            payment_method = "cash",
            items = items,
            condition = _condition.value.ifBlank { null }
        )

        viewModelScope.launch {
            _loading.value = true
            runCatching { api.createSale(body) }
                .onSuccess {
                    _toast.value = it.message ?: "Sale completed"
                    onSuccess()
                }
                .onFailure { t ->
                    Timber.e(t, "Sale failed")
                    _toast.value = "Failed to complete sale"
                }
            _loading.value = false
        }
    }
}
