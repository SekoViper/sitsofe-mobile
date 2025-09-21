package com.sitsofe.scanner.di

import android.content.Context
import com.sitsofe.scanner.BuildConfig
import com.sitsofe.scanner.core.auth.Auth
import com.sitsofe.scanner.core.data.ProductRepository
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
        val auth = Interceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .apply {
                    Auth.token?.let { header("Authorization", "Bearer $it") }
                }
                .build()
            Timber.tag("HTTP").d("→ %s %s", req.method, req.url)
            chain.proceed(req)
        }

        val httpLogger = HttpLoggingInterceptor { msg ->
            Timber.tag("HTTP").d(msg)   // logs request/response lines + body
        }.apply { level = HttpLoggingInterceptor.Level.BODY }

        return OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(httpLogger)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(): Retrofit =
        retrofitRef ?: Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) // e.g., http://192.168.1.99:5001/api/
            .client(http())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .also { retrofitRef = it }

    fun api(): Api = apiRef ?: retrofit().create(Api::class.java).also { apiRef = it }

    fun db(ctx: Context): AppDb = dbRef ?: AppDb.get(ctx).also { dbRef = it }

}
