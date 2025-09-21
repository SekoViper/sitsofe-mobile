package com.sitsofe.scanner.core.network

import com.sitsofe.scanner.BuildConfig
import com.sitsofe.scanner.core.auth.Auth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Network {
    val api: Api by lazy {
        val authHeader = Interceptor { chain ->
            val b = chain.request().newBuilder()
            Auth.token?.let { b.header("Authorization", "Bearer $it") }
            Auth.tenantId?.let { b.header("X-Tenant-Id", it) }
            Auth.subsidiaryId?.let { b.header("X-Subsidiary-Id", it) }
            chain.proceed(b.build())
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authHeader)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) // you already set this in flavors
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)
    }
}
