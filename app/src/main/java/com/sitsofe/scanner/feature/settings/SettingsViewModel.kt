package com.sitsofe.scanner.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.sitsofe.scanner.core.auth.Auth
import com.sitsofe.scanner.core.auth.SessionPrefs
import com.sitsofe.scanner.di.ServiceLocator
import timber.log.Timber

class SettingsViewModel(private val appContext: Context) : ViewModel() {

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
        try { ServiceLocator.db(appContext).clearAllTables() }
        catch (t: Throwable) { Timber.w(t, "clearAllTables failed (continuing logout)") }

        SessionPrefs.clear(appContext)
        Auth.updateFrom(null)
    }

    fun switchAccount(preserveEmail: String?) {
        try { ServiceLocator.db(appContext).clearAllTables() }
        catch (t: Throwable) { Timber.w(t, "clearAllTables failed (continuing switchAccount)") }

        val last = preserveEmail?.trim().orEmpty()
        SessionPrefs.clear(appContext)
        if (last.isNotEmpty()) SessionPrefs.setLastEmail(appContext, last)
        Auth.updateFrom(null)
    }
}
