package com.sitsofe.scanner.core.network
import com.sitsofe.scanner.core.db.ProductEntity

data class ProductDto(
    val _id: String,
    val name: String,
    val price: Double,
    val discount_price: Double = 0.0,
    val cost_price: Double,
    val stock: Double,
    val barcode: String? = null,
    val category_name: String? = null
)

fun ProductDto.toEntity() = ProductEntity(
    id = _id,
    name = name,
    price = price,
    costPrice = cost_price,
    stock = stock,
    barcode = barcode,
    categoryName = category_name
)
