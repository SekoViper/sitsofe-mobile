// core/auth/LogoutUseCase.kt
package com.sitsofe.scanner.core.auth

import android.content.Context

class LogoutUseCase(private val ctx: Context) {
    operator fun invoke() {
        SessionPrefs.clear(ctx)
        Auth.updateFrom(null)
    }
}
