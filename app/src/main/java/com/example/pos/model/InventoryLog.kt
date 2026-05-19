package com.example.pos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InventoryLog(

    val id: String,

    val quantity: Double,

    @SerialName("stock_before")
    val stockBefore: Double,

    @SerialName("stock_after")
    val stockAfter: Double,

    val type: String,

    val description: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("products_id")
    val productsId: String,

    /*
     * Nama produk hasil join
     */
    @SerialName("product_name")
    val productName: String
)