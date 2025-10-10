package com.sitsofe.scanner.core.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Retrofit endpoints used by the app. */
interface Api {

    // ── PRODUCTS (Subsidiary-scoped) ───────────────────────────────────────────
    // Example: http://192.168.1.99:5001/api/products/subsidiary_products
    @GET("products/subsidiary_products")
    suspend fun subsidiaryProducts(): List<SubsidiaryProductDto>

    // ── CUSTOMERS / SALES (already in use) ────────────────────────────────────
    @GET("customers")
    suspend fun customers(): List<CustomerDto>

    @POST("sales")
    suspend fun createSale(@Body body: SalesRequest): SalesResponse
}

/* ====== DTOs ====== */

/** Matches the JSON you posted for /products/subsidiary_products */
data class SubsidiaryProductDto(
    @SerializedName("_id") val id: String,
    @SerializedName("tenant_id") val tenantId: String?,
    @SerializedName("subsidiary_id") val subsidiaryId: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("price") val price: Double,          // server provides a number; Gson handles int->double
    @SerializedName("discount_price") val discountPrice: Double?,
    @SerializedName("cost_price") val costPrice: Double?,
    @SerializedName("stock") val stock: Double,          // server shows 32; Double keeps UI code unchanged
    @SerializedName("barcode") val barcode: String?,
    @SerializedName("expiry_date") val expiryDate: String?,
    @SerializedName("supplier_name") val supplierName: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("point_allocation_method") val pointAllocationMethod: String?,
    @SerializedName("percentage_rate") val percentageRate: Double?,
    @SerializedName("fixed_points") val fixedPoints: Double?,
    @SerializedName("bonus_points") val bonusPoints: Double?
)

/* ====== Mappers ====== */

/** Clean server strings that may literally be "null" into real nulls/empties. */
private fun String?.nullIfLiteral(): String? =
    this?.takeIf { it.isNotBlank() && it.lowercase() != "null" }

/** Map network -> Room entity fields used by the app. */
fun SubsidiaryProductDto.toEntity(): com.sitsofe.scanner.core.db.ProductEntity =
    com.sitsofe.scanner.core.db.ProductEntity(
        id = id,
        name = name,
        barcode = barcode.nullIfLiteral() ?: "",
        price = price,
        stock = stock,
        costPrice = costPrice ?: 0.0,
        categoryName = categoryName.nullIfLiteral()
    )

/* ====== Customers / Sales models (as used earlier) ====== */

data class CustomerDto(
    @SerializedName("_id") val _id: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String
)

data class SalesItem(
    val product_id: String,
    val quantity: Int,
    val price: Double,
    val name: String
)

data class SalesRequest(
    val customer_phone: String?,        // null for internal sales
    val customer_type: String,          // "regular" or "owner"
    val payment_method: String,         // "cash" for now
    val items: List<SalesItem>,
    val condition: String? = null
)

data class SalesResponse(
    val id: String? = null,
    val message: String? = null
)
