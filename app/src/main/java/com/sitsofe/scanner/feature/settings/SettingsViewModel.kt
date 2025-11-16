package com.sitsofe.scanner.feature.settings

import androidx.lifecycle.ViewModel
import com.sitsofe.scanner.core.auth.Auth
import com.sitsofe.scanner.core.auth.SessionPrefs
import com.sitsofe.scanner.core.db.AppDb
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionPrefs: SessionPrefs,
    private val db: AppDb
) : ViewModel() {

    data class Profile(
        val role: String?,
        val currency: String?,
        val name: String?,
        val phone: String?,
        val company: String?,
        val email: String?
    )

    fun profile(): Profile = Profile(
        role = Auth.role,
        currency = Auth.currency,
        name = Auth.userName,
        phone = Auth.userPhone,
        company = Auth.userCompany,
        email = Auth.userEmail
    )

    fun logout() {
        try { db.clearAllTables() }
        catch (t: Throwable) { Timber.w(t, "clearAllTables failed (continuing logout)") }

        sessionPrefs.clear()
        Auth.updateFrom(null)
    }

    fun switchAccount(preserveEmail: String?) {
        try { db.clearAllTables() }
        catch (t: Throwable) { Timber.w(t, "clearAllTables failed (continuing switchAccount)") }

        val last = preserveEmail?.trim().orEmpty()
        sessionPrefs.clear()
        if (last.isNotEmpty()) sessionPrefs.setLastEmail(last)
        Auth.updateFrom(null)
    }
}
