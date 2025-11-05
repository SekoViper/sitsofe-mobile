package com.sitsofe.scanner.core.auth

object Auth {
    var token: String? = null

    var tenantId: String? = null
    var subsidiaryId: String? = null

    var userId: String? = null
    var role: String? = null
    var currency: String? = null

    // NEW user profile fields
    var userName: String? = null
    var userPhone: String? = null
    var userCompany: String? = null
    var userEmail: String? = null

    val isLoggedIn: Boolean get() = !token.isNullOrBlank()

    fun updateFrom(session: Session?) {
        if (session == null) {
            token = null
            tenantId = null
            subsidiaryId = null
            userId = null
            role = null
            currency = null
            userName = null
            userPhone = null
            userCompany = null
            userEmail = null
            return
        }
        token = session.token
        tenantId = session.tenantId
        subsidiaryId = session.subsidiaryId
        userId = session.userId
        role = session.role
        currency = session.currency
        userName = session.userName
        userPhone = session.userPhone
        userCompany = session.userCompany
        userEmail = session.userEmail
    }

    fun isAllowed(): Boolean = role == "subsidiary_admin" || role == "pharmacist"
}
