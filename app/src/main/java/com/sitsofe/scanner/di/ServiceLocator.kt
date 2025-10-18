package com.sitsofe.scanner.di

import android.content.Context
import com.sitsofe.scanner.BuildConfig
import com.sitsofe.scanner.core.auth.Auth
import com.sitsofe.scanner.core.db.AppDb
import com.sitsofe.scanner.core.network.Api
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

object ServiceLocator {
    @Volatile private var retrofitRef: Retrofit? = null
    @Volatile private var apiRef: Api? = null
    @Volatile private var dbRef: AppDb? = null

    private fun http(): OkHttpClient {
        val identityHeaders = Interceptor { chain ->
            val b = chain.request().newBuilder()
                .header("Accept", "application/json")

            Auth.token?.let { b.header("Authorization", "Bearer $it") }
            Auth.tenantId?.let { b.header("X-Tenant-Id", it) }
            Auth.subsidiaryId?.let { b.header("X-Subsidiary-Id", it) }
            Auth.userId?.let { b.header("X-User-Id", it) }
            Auth.role?.let { b.header("X-Role", it) }
            Auth.currency?.let { b.header("X-Currency", it) }

            val req = b.build()
            Timber.tag("HTTP").d("→ %s %s", req.method, req.url)
            chain.proceed(req)
        }

        val httpLogger = HttpLoggingInterceptor { msg -> Timber.tag("HTTP").d(msg) }
            .apply { level = HttpLoggingInterceptor.Level.BODY }

        return OkHttpClient.Builder()
            .addInterceptor(identityHeaders)
            .addInterceptor(httpLogger)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(): Retrofit =
        retrofitRef ?: Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(http())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .also { retrofitRef = it }

    fun api(): Api = apiRef ?: retrofit().create(Api::class.java).also { apiRef = it }

    fun db(ctx: Context): AppDb = dbRef ?: AppDb.get(ctx).also { dbRef = it }
}
