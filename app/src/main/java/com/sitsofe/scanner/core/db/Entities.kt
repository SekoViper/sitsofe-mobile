package com.sitsofe.scanner.core.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val costPrice: Double,
    val stock: Double,
    val barcode: String,
    val categoryName: String?
)

fun ProductEntity.pretty(): String =
    "GHS ${price} • Stock: ${stock.toInt()}"
