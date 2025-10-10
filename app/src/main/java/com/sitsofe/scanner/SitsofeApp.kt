package com.sitsofe.scanner

import android.app.Application
import com.sitsofe.scanner.core.auth.Auth
import com.sitsofe.scanner.core.auth.SessionPrefs
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SitsofeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        // Load persisted session (if any) into Auth for interceptors.
        val session = SessionPrefs.load(this)
        Auth.updateFrom(session)

        Timber.tag("APP").i(
            "Boot. baseUrl=%s loggedIn=%s role=%s tenant=%s subsidiary=%s",
            BuildConfig.BASE_URL, Auth.isLoggedIn, Auth.role, Auth.tenantId, Auth.subsidiaryId
        )
    }
}
