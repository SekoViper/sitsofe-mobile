package com.sitsofe.scanner.core.auth

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class Session(
    val token: String,
    val tenantId: String,
    val subsidiaryId: String,
    val userId: String,
    val role: String,
    val currency: String,
    val userName: String? = null,
    val userPhone: String? = null,
    val userCompany: String? = null,
    val userEmail: String? = null
)

@Singleton
open class SessionPrefs @Inject constructor(@ApplicationContext private val context: Context) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    fun save(s: Session) {
        prefs.edit()
            .putString(K_TOKEN, s.token)
            .putString(K_TENANT, s.tenantId)
            .putString(K_SUBSIDIARY, s.subsidiaryId)
            .putString(K_USER, s.userId)
            .putString(K_ROLE, s.role)
            .putString(K_CURRENCY, s.currency)
            // NEW
            .putString(K_USER_NAME, s.userName)
            .putString(K_USER_PHONE, s.userPhone)
            .putString(K_USER_COMPANY, s.userCompany)
            .putString(K_USER_EMAIL, s.userEmail)
            .apply()
    }

    fun load(): Session? {
        val token = prefs.getString(K_TOKEN, null) ?: return null
        val tenant = prefs.getString(K_TENANT, null) ?: return null
        val sub = prefs.getString(K_SUBSIDIARY, null) ?: return null
        val user = prefs.getString(K_USER, null) ?: return null
        val role = prefs.getString(K_ROLE, null) ?: return null
        val curr = prefs.getString(K_CURRENCY, null) ?: "USD"

        val name = prefs.getString(K_USER_NAME, null)
        val phone = prefs.getString(K_USER_PHONE, null)
        val company = prefs.getString(K_USER_COMPANY, null)
        val email = prefs.getString(K_USER_EMAIL, null)

        return Session(
            token = token,
            tenantId = tenant,
            subsidiaryId = sub,
            userId = user,
            role = role,
            currency = curr,
            userName = name,
            userPhone = phone,
            userCompany = company,
            userEmail = email
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun setLastEmail(email: String) {
        prefs.edit().putString(K_LAST_EMAIL, email).apply()
    }

    fun getLastEmail(): String? =
        prefs.getString(K_LAST_EMAIL, null)

    companion object {
        private const val NAME = "session_prefs"
        private const val K_TOKEN = "token"
        private const val K_TENANT = "tenant_id"
        private const val K_SUBSIDIARY = "subsidiary_id"
        private const val K_USER = "user_id"
        private const val K_ROLE = "role"
        private const val K_CURRENCY = "currency"

        // NEW: persisted user profile keys
        private const val K_USER_NAME = "user_name"
        private const val K_USER_PHONE = "user_phone"
        private const val K_USER_COMPANY = "user_company"
        private const val K_USER_EMAIL = "user_email"

        // last used login email (for “Switch account” prefill)
        private const val K_LAST_EMAIL = "last_email"
    }
}
