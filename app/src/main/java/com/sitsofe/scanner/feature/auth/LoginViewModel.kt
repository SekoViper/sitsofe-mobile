package com.sitsofe.scanner.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sitsofe.scanner.core.auth.Auth
import com.sitsofe.scanner.core.auth.Session
import com.sitsofe.scanner.core.auth.SessionPrefs
import com.sitsofe.scanner.core.network.Api
import com.sitsofe.scanner.core.network.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: Api,
    private val sessionPrefs: SessionPrefs
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (_loading.value) return
        _error.value = null
        _loading.value = true

        viewModelScope.launch {
            runCatching {
                val res = api.login(LoginRequest(email = email, password = password))
                val session = Session(
                    token        = res.token,
                    tenantId     = res.user.tenantId,
                    subsidiaryId = res.user.subsidiaryId,
                    userId       = res.user.id,
                    role         = res.user.role,
                    currency     = res.tenant.currency ?: "USD",
                    userName     = res.user.name,
                    userPhone    = res.user.phone,
                    userCompany  = res.tenant.name,
                    userEmail    = res.user.email
                )
                sessionPrefs.save(session)
                Auth.updateFrom(session)
                sessionPrefs.setLastEmail(email)
            }.onSuccess { onSuccess() }
             .onFailure { t ->
                 Timber.e(t, "Login failed")
                 _error.value = "Login failed. Check your email/password."
             }
            _loading.value = false
        }
    }
}
