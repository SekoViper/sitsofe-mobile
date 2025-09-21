package com.sitsofe.scanner.core.network

import retrofit2.http.GET

interface Api {
    @GET("products/subsidiary_products")
    suspend fun productsFull(): List<ProductDto>

    @GET("products/subsidiary_products_name_and_id")
    suspend fun productsMin(): List<ProductNameIdDto>
}

