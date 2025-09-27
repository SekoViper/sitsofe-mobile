package com.sitsofe.scanner.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {

    @GET("customers")
    suspend fun customers(): List<CustomerDto>

    @POST("sales")
    suspend fun createSale(@Body body: SalesRequest): SalesResponse
}

data class CustomerDto(
    val _id: String,
    val name: String,
    val phone: String
)

data class SalesItem(
    val product_id: String,
    val quantity: Int,
    val price: Double,
    val name: String
)

data class SalesRequest(
    val customer_phone: String?,        // null for internal sales
    val customer_type: String,          // "regular" or "owner" for internal
    val payment_method: String,         // "cash" by default for now
    val items: List<SalesItem>,
    val condition: String? = null       // optional notes/condition text
)

data class SalesResponse(
    val id: String? = null,
    val message: String? = null
)
