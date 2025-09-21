package com.sitsofe.scanner

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.sitsofe.scanner.core.auth.Auth

@HiltAndroidApp
class SitsofeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Debug logging
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        Timber.tag("APP").i(
            "Boot. baseUrl=%s role=%s tenant=%s subsidiary=%s",
            BuildConfig.BASE_URL, Auth.role, Auth.tenantId, Auth.subsidiaryId
        )
    }
}
