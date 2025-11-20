package com.sitsofe.scanner.feature.customers

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sitsofe.scanner.core.data.toDto
import com.sitsofe.scanner.core.data.toEntity
import com.sitsofe.scanner.core.db.AppDb
import com.sitsofe.scanner.core.network.ApiMessage
import com.sitsofe.scanner.core.network.CustomerDto
import com.sitsofe.scanner.core.network.UpdateCustomerRequest
import com.sitsofe.scanner.di.ServiceLocator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class CustomersViewModel(
    private val appContext: Context
) : ViewModel() {

    private val api = ServiceLocator.api()
    private val customerDao = AppDb.get(appContext).customerDao()

    private val _customers = MutableStateFlow<List<CustomerDto>>(emptyList())
    val customers: StateFlow<List<CustomerDto>> = _customers.asStateFlow()

    private val _listLoading = MutableStateFlow(false)
    val listLoading: StateFlow<Boolean> = _listLoading.asStateFlow()

    private val _detailLoading = MutableStateFlow(false)
    val detailLoading: StateFlow<Boolean> = _detailLoading.asStateFlow()

    private val _deleting = MutableStateFlow(false)
    val deleting: StateFlow<Boolean> = _deleting.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _detailError = MutableStateFlow<String?>(null)
    val detailError: StateFlow<String?> = _detailError.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerDto?>(null)
    val selectedCustomer: StateFlow<CustomerDto?> = _selectedCustomer.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun loadCustomers(force: Boolean = false) {
        if (_listLoading.value) return
        viewModelScope.launch {
            _listLoading.value = true

            // Serve cached customers first for a fast first paint.
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
                        _events.emit("Failed to load customers")
                    } else {
                        _events.emit("Showing cached customers")
                    }
                }

            _listLoading.value = false
        }
    }

    fun loadCustomerDetail(id: String) {
        viewModelScope.launch {
            _detailLoading.value = true
            _detailError.value = null
            runCatching { api.customerDetail(id) }
                .onSuccess { detail ->
                    _selectedCustomer.value = detail
                    // merge into list so the latest info shows in the cards
                    _customers.update { current ->
                        val idx = current.indexOfFirst { it.id == id }
                        if (idx == -1) current + detail
                        else current.toMutableList().also { it[idx] = detail }
                    }
                }
                .onFailure { t ->
                    Timber.e(t, "Failed to load customer detail")
                    _detailError.value = "Unable to load customer"
                    _events.emit("Unable to load customer")
                }
            _detailLoading.value = false
        }
    }

    fun deleteCustomer(id: String, onDeleted: () -> Unit) {
        if (_deleting.value) return
        viewModelScope.launch {
            _deleting.value = true
            runCatching { api.deleteCustomer(id) }
                .onSuccess { resp: ApiMessage ->
                    _customers.update { list -> list.filterNot { it.id == id } }
                    if (_selectedCustomer.value?.id == id) _selectedCustomer.value = null
                    runCatching { customerDao.deleteById(id) }
                    _events.emit(resp.message ?: "Customer deleted")
                    onDeleted()
                }
                .onFailure { t ->
                    Timber.e(t, "Failed to delete customer")
                    _events.emit("Failed to delete customer")
                }
            _deleting.value = false
        }
    }

    fun updateCustomer(id: String, name: String, phone: String, onUpdated: () -> Unit) {
        if (_saving.value) return
        viewModelScope.launch {
            _saving.value = true
            runCatching { api.updateCustomer(id, UpdateCustomerRequest(name, phone)) }
                .onSuccess { resp ->
                    val updated = resp.customer
                    if (updated != null) {
                        _selectedCustomer.value = updated
                        _customers.update { current ->
                            val idx = current.indexOfFirst { it.id == id }
                            if (idx == -1) current + updated
                            else current.toMutableList().also { it[idx] = updated }
                        }
                        runCatching { customerDao.upsertAll(listOf(updated.toEntity())) }
                    }
                    _events.emit(resp.message ?: "Customer updated")
                    onUpdated()
                }
                .onFailure { t ->
                    Timber.e(t, "Failed to update customer")
                    _events.emit("Failed to update customer")
                }
            _saving.value = false
        }
    }
}
